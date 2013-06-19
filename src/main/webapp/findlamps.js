function drawLampsTable(lamps) {
    var lampsTable = document.getElementById("lamps-table");
    // Delete lamps and add again
    lampsTable.innerHTML = "";

    for (var i = 0; i < lamps.length; i++) {
        var tr = document.createElement('tr');

        var lamp = lamps[i];
        var mac = lamp.macAddress;
        var ip = lamp.ipAddress;
        var name = lamp.name;
        var jobs = lamp.jobs.join(", ");

        var macTd = document.createElement('td');
        macTd.innerHTML = mac;
        tr.appendChild(macTd);

        var ipTd = document.createElement('td');
        ipTd.innerHTML = ip;
        tr.appendChild(ipTd);

        var nameTd = document.createElement('td');
        nameTd.innerHTML = '<span id="lamp-name-' + mac + '">' + name + '</span> <button onclick="changeLampName(\'' + mac + '\',\'' + name + '\');">edit</button>';
        tr.appendChild(nameTd);

        var jobsTd = document.createElement('td');
        jobsTd.innerHTML = '<span id="lamp-name-' + mac + '-jobs">' + jobs + '</span>';
        tr.appendChild(jobsTd);

        lampsTable.appendChild(tr);

    }
}

function updateLampTable(project, mac){
    var lampTD = document.getElementById("lamp-name-" + mac + "-jobs");
    it.getLamp(mac, function(t) {
        var lamp = t.responseObject();
        var jobs = lamp.jobs.join(", ");
        lampTD.innerHTML = '<span id="lamp-name-' + mac + '-jobs">' + jobs + '</span>';
    });

}

function updateJobTableHeader(mac, name){
    var lampTH = document.getElementById("lamp-job-name-" + mac);
    lampTH.innerHTML = '<span id="lamp-job-name-' + mac + '">' + name + '</span>';
}

function updateLampJobsView(project, mac){
    saveLampJobAssociation(project, mac);
    updateLampTable(project, mac);
}

function saveLampJobAssociation(project, macAddress) {
    var checkbox = document.getElementById("lamp-" + macAddress + "-" + project);
    if (checkbox){
        var checked = checkbox.checked;
        if (checked) {
            it.addProjectToLamp(
                project,
                macAddress,
                notificationBar.show('Job added to lamp', notificationBar.OK));
        } else {
            it.removeProjectFromLamp(
                project,
                macAddress,
                notificationBar.show('Job removed from lamp', notificationBar.OK));
        }
    } else {
    }
}

function drawLampsJobsTable(lamps) {
    var table = document.getElementById("lamp-job");
    table.innerHTML = "";

    it.getProjects(function(t) {
        var projects = t.responseObject();

        var tr = document.createElement('tr');
        var projectNameCell = document.createElement('th');
        projectNameCell.innerHTML = "Job";
        tr.appendChild(projectNameCell);
        for (var i = 0; i < lamps.length; i++) {
            var lampNameCell = document.createElement('th');
            lampName = lamps[i].name;
            if (!lampName){
                lampName = lamps[i].macAddress;
            }
            lampNameCell.innerHTML = '<span id="lamp-job-name-' + lamps[i].macAddress + '">' + lampName + '</span>';

            tr.appendChild(lampNameCell);
        }
        table.appendChild(tr);

        for (var i = 0; i < projects.length; i++) {
            var tr = document.createElement('tr');

            var jobNameCell = document.createElement('th');
            jobNameCell.innerHTML = projects[i];
            tr.appendChild(jobNameCell);

            for (var j = 0; j < lamps.length; j++) {
                var cell = document.createElement('td');
                var checkbox = document.createElement('input');
                checkbox.type = "checkbox";
                checkbox.checked = lamps[j].jobs.indexOf(projects[i]) > -1 ? true : false;
                checkbox.id = "lamp-" + lamps[j].macAddress + "-" + projects[i];
                checkbox.setAttribute("onclick", "updateLampJobsView('" + projects[i] + "', '" + lamps[j].macAddress + "')" );
                cell.appendChild(checkbox);
                tr.appendChild(cell);
            }

            table.appendChild(tr);
        }
    });
}

function changeLampName(mac, oldName) {
    var name=prompt("Please enter the new name of the lamp", oldName);
    if (name != null && name != "") {
        it.changeLampName(mac, name, function(t) {
            var result = t.responseObject();
            if(result) {
                nameSpan = document.getElementById("lamp-name-" + mac);
                nameSpan.innerHTML = name;
                updateJobTableHeader(mac, name);
                // Update button, Two next sibling due to space between name and button
                button = nameSpan.nextSibling.nextSibling;
                button.setAttribute("onclick", "changeLampName('" + mac + "','" + name + "')");
                notificationBar.show('Lamp name changed successfully',notificationBar.OK);
            } else {
                notificationBar.show('An error occurred while changing the name',notificationBar.ERROR);
            }
        });
    } else {
        notificationBar.show('The name of a lamp cannot be empty',notificationBar.ERROR);
    }
}

function findlamps() {
    var button = document.getElementById("button");
    var spinner = document.getElementById("spinner");

    button.style.display = "none";
    spinner.style.display = "block";

    it.findLamps(function(t) {
        button.style.display = "block";
        spinner.style.display = "none";

        var lamps = t.responseObject();

        if (lamps.length === 0) {
            notificationBar.show('No lamps have been found',notificationBar.WARNING)
        } else {
            drawLampsTable(lamps);
            drawLampsJobsTable(lamps);
        }
    });
}

function addLamp() {
    var button = document.getElementById("add-lamp");
    var spinner = document.getElementById("spinner2");

    button.style.display = "none";
    spinner.style.display = "block";

    var ipAddress = document.getElementById("add-lamp-input").value;

    it.addLampByIpAddress(ipAddress, function(t) {
        button.style.display = "block";
        spinner.style.display = "none";

        var lamps = t.responseObject();

        if (lamps != null) {
            drawLampsTable(lamps);
            drawLampsJobsTable(lamps);
        } else {
            notificationBar.show('Lamp not found', notificationBar.WARNING)
        }

    });
}

function resetLamps() {
    it.resetLamps(function(t) {
        var result = t.responseObject;
        if (result) {
            lamps = [];
            drawLampsTable(lamps);
            drawLampsJobsTable(lamps);
        } else {
            notificationBar.show('An error occurred resetting the lamps list', notificationBar.WARNING);
        }
    });
}

function init() {
    it.getLamps(function(t) {
        var lamps = t.responseObject();
        if(lamps.length > 0) {
             drawLampsTable(lamps);
             drawLampsJobsTable(lamps);
        }

    });
}

YAHOO.util.Event.onDOMReady(init);