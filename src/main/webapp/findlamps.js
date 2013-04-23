function drawLampsTable(lamps) {
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
        nameTd.innerHTML = '<span id="lamp-name-' + mac + '">' + name + '</span> <button onclick="changeLampName(\'' + mac + '\');">edit</button>';
        tr.appendChild(nameTd);

        var jobsTd = document.createElement('td');
        jobsTd.innerHTML = jobs;
        tr.appendChild(jobsTd);

        var lampsTable = document.getElementById("lamps-table");
        lampsTable.innerHTML = "";
        lampsTable.appendChild(tr);
    }
}

function saveLampJobAssociation(project, macAddress) {
    var checkbox = document.getElementById("lamp-" + macAddress + "-" + project);
    var checked = checkbox.checked === "yes" ? true : false;
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
}

function drawLampsJobsTable(lamps) {
    var table = document.getElementById("lamp-job");

    it.getProjects(function(t) {
        var projects = t.responseObject();

        var tr = document.createElement('tr');
        for (var i = 0; i < lamps.length; i++) {
            var lampNameCell = document.createElement('th');
            lampNameCell.innerHTML = lamps[i].name;
            tr.appendChild(lampNameCell);
        }

        for (var i = 0; i < projects.length; i++) {
            var tr = document.createElement('tr');

            var jobNameCell = document.createElement('th');
            jobNameCell.innerHTML = projects[i];
            tr.appendChild(jobNameCell);

            for (var j = 0; j < lamps.length; j++) {
                var cell = document.createElement('td');
                var checkbox = document.createElement('input');
                checkbox.type = "checkbox";
                checkbox.checked = lamps.jobs.indexOf(projects[i]) > -1 ? "yes" : "no";
                checkbox.id = "lamp-" + lamp.macAddress + "-" + projects[i];
                checkbox.onclick = saveLampJobAssociation(projects[i], lamps[j].macAddress);
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