document.addEventListener('DOMContentLoaded', function() {
    loadFolders();

    // Adding event listeners for folder and document operations
    document.getElementById('addFolderButton').addEventListener('click', () => addRootFolder());
    document.getElementById('logoutButton').addEventListener('click', logout);
    
    // Adding event listeners for the bin
    const bin = document.getElementById('bin');
    bin.addEventListener('dragover', handleBinDragOver);
    bin.addEventListener('drop', handleBinDrop);
});

function handleBinDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
}

function handleBinDrop(event) {
    event.preventDefault();
    const itemId = event.dataTransfer.getData('text/plain');
    const itemType = event.dataTransfer.getData('itemType');
    console.log(`Dropped ${itemType} with ID: ${itemId} into bin`);

    if (itemType === 'document') {
        deleteDocument(itemId);
    } else if (itemType === 'folder') {
        deleteFolder(itemId);
    }
}

function handleDragStart(event, itemId, itemType) {
    event.stopPropagation(); // Prevent event bubbling
    event.dataTransfer.setData('text/plain', itemId);
    event.dataTransfer.setData('itemType', itemType);
    console.log(`Dragging ${itemType} with ID: ${itemId}`);
}

function handleDrop(event, folderId) {
    event.preventDefault();
    const itemId = event.dataTransfer.getData('text/plain');
    const itemType = event.dataTransfer.getData('itemType');
    if (itemType === 'document') {
        console.log(`Dropped document with ID: ${itemId} into folder with ID: ${folderId}`);
        moveDocument(itemId, folderId);
    }
}

function loadFolders() {
    console.log("Loading folders...");
    fetch('/tiw_js/api/folders')
        .then(response => response.json())
        .then(data => {
            const folderTree = buildFolderTree(data); // Convert flat list to tree if necessary
            const folderList = document.getElementById('folderList');
            displayFolders(folderTree, folderList); // Start with root level
        })
        .catch(error => console.error('Error loading folders:', error));
}

function buildFolderTree(folders) {
    let map = {}, node, roots = [], i;

    // First pass to initialize the map and prepare subfolders array
    for (i = 0; i < folders.length; i++) {
        map[folders[i].id] = i;
        folders[i].subfolders = [];
    }

    // Second pass to populate children or identify roots
    for (i = 0; i < folders.length; i++) {
        node = folders[i];
        if (node.parentId !== null && map[node.parentId] !== undefined) {
            folders[map[node.parentId]].subfolders.push(node);
        } else {
            roots.push(node);
        }
    }

    return roots;
}

function addRootFolder() {
    const folderName = prompt("Enter new root folder name:");
    if (!folderName) return;

    console.log("Adding root folder with name:", folderName);

    const bodyData = `name=${encodeURIComponent(folderName)}&parentId=null`;

    fetch('/tiw_js/api/folders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: bodyData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to add root folder');
        }
        return response.text();
    })
    .then(data => {
        console.log("Root folder added response:", data);
        alert("Root folder added successfully!");
        loadFolders();  // Reload folders to show the new addition
    })
    .catch(error => {
        console.error('Error adding root folder:', error);
        alert("Error adding root folder: " + error);
    });
}

function addSubfolder(parentId) {
    const folderName = prompt("Enter new subfolder name:");
    if (!folderName) return;

    console.log("Adding subfolder with name:", folderName, " under parent ID:", parentId);

    const bodyData = `name=${encodeURIComponent(folderName)}&parentId=${parentId}`;

    fetch('/tiw_js/api/folders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: bodyData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to add subfolder');
        }
        return response.text();
    })
    .then(data => {
        console.log("Subfolder added response:", data);
        alert("Subfolder added successfully!");
        loadFolders();  // Reload folders to show the new addition
    })
    .catch(error => {
        console.error('Error adding subfolder:', error);
        alert("Error adding subfolder: " + error);
    });
}

function displayFolders(folders, parentElement, level = 0) {
    parentElement.innerHTML = ''; // Clear existing folders

    folders.forEach(folder => {
        const li = document.createElement('li');
        li.style.marginLeft = `${level * 20}px`; // Add indentation based on level

        const folderLink = document.createElement('a');
        folderLink.href = '#';
        folderLink.textContent = folder.name;
        folderLink.dataset.folderId = folder.id;
        folderLink.addEventListener('dragover', handleDragOver);
        folderLink.addEventListener('drop', (event) => handleDrop(event, folder.id));
        li.appendChild(folderLink);

        // Add Subfolder Button
        const addSubfolderButton = document.createElement('button');
        addSubfolderButton.textContent = 'Add Subfolder';
        addSubfolderButton.onclick = () => addSubfolder(folder.id); // Pass parentId explicitly
        li.appendChild(addSubfolderButton);

        // Add Document Button
        const addDocumentButton = document.createElement('button');
        addDocumentButton.textContent = 'Add Document';
        addDocumentButton.onclick = () => addDocument(folder.id);
        li.appendChild(addDocumentButton);

        // Append the folder list item to the parent element
        parentElement.appendChild(li);

        // Create an unordered list for the subfolders and documents
        const ul = document.createElement('ul');
        li.appendChild(ul);

        // Load and display documents for this folder
        loadDocuments(folder.id, ul, level + 1);

        // Check if this folder has subfolders and recursively display them
        if (folder.subfolders && folder.subfolders.length > 0) {
            displayFolders(folder.subfolders, ul, level + 1); // Increase level for subfolders
        }

        // Set draggable attribute and dragstart event for folders
        li.setAttribute('draggable', 'true');
        li.addEventListener('dragstart', (event) => handleDragStart(event, folder.id, 'folder'));
    });
}

function handleDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
}

function loadDocuments(folderId, parentElement, level) {
    if (!parentElement) {
        console.error("Parent element is undefined for folder ID:", folderId);
        return;
    }

    console.log("Loading documents for folder ID:", folderId);

    fetch(`/tiw_js/api/documents?folderId=${folderId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch'); // Throws if the HTTP status code is not in the 200 range
            }
            return response.json();
        })
        .then(data => {
            console.log("Documents loaded for folder ID:", folderId, data); // Debug log
            displayDocuments(data, parentElement, level);
        })
        .catch(error => {
            console.error('Error loading documents:', error);
        });
}

function viewDocument(docId) {
    fetch(`/tiw_js/api/documents/${docId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch document details');
            }
            return response.json();
        })
        .then(document => {
            displayDocumentDetails(document);
        })
        .catch(error => {
            console.error('Error fetching document details:', error);
            alert("Failed to fetch document details: " + error.message);
        });
}

function displayDocumentDetails(document) {
    const documentDetailsSection = window.document.getElementById('documentDetails');
    documentDetailsSection.innerHTML = `
        <h3>${document.name}</h3>
        <p><strong>Type:</strong> ${document.type}</p>
        <p><strong>Summary:</strong> ${document.summary}</p>
        <p><strong>Created by:</strong> ${document.userId}</p>
        <p><strong>Created at:</strong> ${document.createdAt}</p>
    `;
}


function displayDocuments(documents, parentElement, level) {
    if (!parentElement) {
        console.error("Parent element is undefined when trying to display documents.");
        return;
    }

    documents.forEach(doc => {
        const li = document.createElement('li');
        li.style.marginLeft = `${level * 20}px`; // Add indentation based on level
        li.setAttribute('draggable', 'true');
        li.addEventListener('dragstart', (event) => handleDragStart(event, doc.id, 'document'));

        const docLink = document.createElement('a');
        docLink.href = '#';
        docLink.textContent = doc.name;
        li.appendChild(docLink);

        // Add Document Actions
        const actionCell = document.createElement('span');
        actionCell.innerHTML = `<button onclick="viewDocument(${doc.id})">View</button>
                                <button onclick="deleteDocument(${doc.id})">Delete</button>`;
        li.appendChild(actionCell);

        parentElement.appendChild(li);
    });
}

function moveDocument(docId, newFolderId) {
    const bodyData = JSON.stringify({ folderId: newFolderId });
    const requestUrl = `/tiw_js/api/documents/${docId}`;

    console.log(`Sending PUT request to ${requestUrl} with data:`, bodyData); // Debug log

    fetch(requestUrl, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: bodyData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Failed to move document'); });
        }
        return response.json();
    })
    .then(result => {
        alert('Document moved successfully:', result);
        loadFolders();  // Reload folders to reflect the changes
    })
    .catch(error => {
        console.error('Error moving document:', error);
        alert('Failed to move document: ' + error.message);
    });
}

function addDocument(folderId) {
    const documentFormHtml = `
        <form id="addDocumentForm">
            <label for="documentName">Name:</label>
            <input type="text" id="documentName" name="name" required><br>
            <label for="documentType">Type:</label>
            <input type="text" id="documentType" name="type" required><br>
            <label for="documentSummary">Summary:</label>
            <textarea id="documentSummary" name="summary" required></textarea><br>
            <button type="submit">Add Document</button>
            <button type="button" id="cancelButton">Cancel</button>
        </form>
    `;

    // Find the parent folder list item to insert the form below
    const folderListItem = document.querySelector(`a[data-folder-id="${folderId}"]`).parentElement;

    // Remove any existing form to avoid multiple forms
    const existingForm = document.getElementById('addDocumentForm');
    if (existingForm) {
        existingForm.parentElement.removeChild(existingForm);
    }

    const formContainer = document.createElement('div');
    formContainer.innerHTML = documentFormHtml;
    folderListItem.appendChild(formContainer);

    const form = document.getElementById('addDocumentForm');
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        const documentName = document.getElementById('documentName').value;
        const documentType = document.getElementById('documentType').value;
        const documentSummary = document.getElementById('documentSummary').value;

        const bodyData = JSON.stringify({
            name: documentName,
            type: documentType,
            summary: documentSummary,
            folderId: folderId
        });

        fetch('/tiw_js/api/documents', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: bodyData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(text => { throw new Error(text.error || 'Failed to add document'); });
            }
            return response.json();
        })
        .then(result => {
            alert("Document added successfully!");
            loadFolders();  // Reload folders to show the new addition
        })
        .catch(error => {
            console.error('Error adding document:', error);
            alert("Failed to add document: " + error.message);
        });
    });

    // Handle the cancel button click
    const cancelButton = document.getElementById('cancelButton');
    cancelButton.addEventListener('click', function() {
        folderListItem.removeChild(formContainer);
    });
}


function deleteDocument(documentId) {
	const confirmation = confirm("Are you sure you want to delete this document?");
    if (!confirmation) {
        return; // Exit the function if the user cancels the deletion
    }
	
    fetch(`/tiw_js/api/documents?id=${documentId}`, {
        method: 'DELETE',
        credentials: 'include' // Ensure cookies are included if sessions are used
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete document');
        }
        return response.json();
    })
    .then(result => {
        console.log("Document deleted successfully:", result);
        loadFolders(); // Reload folders to reflect the change
    })
    .catch(error => {
        console.error('Error deleting document:', error);
        alert("Failed to delete document: " + error.message);
    });
}

function deleteFolder(folderId) {
	
	const confirmation = confirm("Are you sure you want to delete this document?");
    if (!confirmation) {
        return; // Exit the function if the user cancels the deletion
    }
    
    fetch(`/tiw_js/api/folders?id=${folderId}`, {
        method: 'DELETE',
        credentials: 'include' // Ensure cookies are included if sessions are used
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete folder');
        }
        return response.json();
    })
    .then(result => {
        alert("Folder deleted successfully!");
        loadFolders(); // Reload folders to reflect the change
    })
    .catch(error => {
        console.error('Error deleting folder:', error);
        alert("Failed to delete folder: " + error.message);
    });
}


function getCurrentFolderId() {
    const folderLinks = document.querySelectorAll('.folder-container a');
    return folderLinks.length ? folderLinks[0].dataset.folderId : null;
}

function logout() {
    fetch('/tiw_js/logout', {
        method: 'GET',
        credentials: 'include' // Ensures cookies are sent along with the request, if sessions are cookie-based
    })
    .then(response => {
        if (response.ok) {
            console.log('Logout successful.');
            window.location.href = 'login.html'; // Redirect on successful logout
        } else {
            // Handle non-successful responses
            return response.text().then(text => { throw new Error(text || 'Logout failed'); });
        }
    })
    .catch(error => {
        console.error('Error during logout:', error);
        alert(error.message || 'Logout failed');
    });
}