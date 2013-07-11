package org.jenkinsci.plugins.extremefeedback.XfManagementLink

import lib.LayoutTagLib
import lib.JenkinsTagLib

def l=namespace(LayoutTagLib)
def t=namespace(JenkinsTagLib)
def st=namespace("jelly:stapler")

l.layout() {
    l.header() {
        st.bind(var: "it", value: my)
        script(src: "/plugin/extreme-feedback/suggest.js")
        script(src: "/plugin/extreme-feedback/angular.min.js")
        script(src: "/plugin/extreme-feedback/findlamps.js")
        link(rel: "stylesheet", type: "text/css", href:"/plugin/extreme-feedback/style.css")
    }
    l.main_panel() {
        div(id:"xf-app-container", "ng-app": "xfApp") {
            div("ng-controller": "xfController") {

                h1 {
                    text(my.displayName)
                }

                div {
                    p {
                        text("Order your lamps at ")
                        a(href: "http://www.gitgear.com/XFD") {
                            text("gitgear.com/XFD")
                        }
                    }
                }

                table(class:"sg-choice") {
                    tr(class: "sg-title") {
                        td(colspan:"2") {
                            text("Add Lamps")
                        }
                    }
                    tr(class: "sg-subtitle") {
                        td {
                            text("Automatically")
                        }
                        td {
                            text("Manually")
                        }
                    }
                    tr {
                        td {
                            div(id: "button", "ng-show":"findlampsToggle") {
                                button("ng-click": "findlamps()", "Find lamps in the subnet")
                            }
                            div(id: "spinner", style: "display: none", "ng-hide": "findlampsToggle") {
                                text("Searching...")
                                t.progressBar(pos:-1)
                            }
                        }
                        td {
                            div(id: "add-lamp", "ng-show":"ipToggle") {
                                input(type: "text", id: "add-lamp-input", "ng-model": "ipAddress", "ng-click": "updateIpContent()")
                                button("ng-click": "addLamp(ipAddress);", "Add Lamp")
                            }
                            div(id: "spinner2", style: "display: none", "ng-hide":"ipToggle") {
                                text("Searching...")
                                t.progressBar(pos:-1)
                            }
                        }
                    }
                }

                table(class:"sg-table", style: "text-align: left", "ng-show": "lamps.length") {
                    thead {
                        tr {
                            th {
                                text("MAC Address")
                            }
                            th {
                                text("IP Address")
                            }
                            th {
                                text("Name")
                            }
                            th {
                                text("Jobs Assigned To")
                            }
                            th {
                                text("Alarm")
                            }
                            th {
                                text("Remove")
                            }
                        }
                    }
                    tbody {
                        tr("ng-repeat": "lamp in lamps") {
                            td {
                                text("{{lamp.macAddress}}")
                            }
                            td {
                                text("{{lamp.ipAddress}}")
                            }
                            td {
                                input(type: "text", "ng-model": "lamp.name", "ng-enter": "changeLamp(lamp)")
                                img(src: "/plugin/extreme-feedback/pencil.png", "ng-click": "changeLamp(lamp)", style: "cursor: pointer;")
                            }
                            td {
                                table(class: "jobs") {
                                    tr("ng-repeat": "job in lamp.jobs", "ng-mouseover": "showRemove = true", "ng-mouseleave": "showRemove = false") {
                                        td {
                                            text("{{job}}")
                                        }
                                        td(align: "right") {
                                            img(src: "/plugin/extreme-feedback/remove.png", "ng-click": "removeProjectFromLamp(job, lamp)", style: "display:none;", "ng-show": "showRemove")
                                        }
                                    }
                                    tr {
                                        td(colspan: "2") {
                                            input(id: "job-{{lamps.indexOf(lamp)}}", type: "text", "ng-click": "suggestProjects(lamps.indexOf(lamp))", "ng-model": "jobName", "ng-enter": "addProjectToLamp(jobName, lamp)")
                                            button("ng-click": "addProjectToLamp(jobName, lamp)", "Add")
                                            div(id: "suggest-{{lamps.indexOf(lamp)}}", style: "display:none;", class: "suggest")
                                        }
                                    }
                                }
                            }
                            td {
                                input(type: "checkbox", "ng-model": "lamp.noisy", "ng-change": "changeLamp(lamp)")
                            }
                            td {
                                img(src: "/plugin/extreme-feedback/remove.png", "ng-click": "removeLamp(lamp)", style: "cursor: pointer;")
                            }
                        }
                    }
                }
                div("ng-show": "!lamps.length") {
                    text("No lamps registered yet.")
                }
            }
        }
    }
}


