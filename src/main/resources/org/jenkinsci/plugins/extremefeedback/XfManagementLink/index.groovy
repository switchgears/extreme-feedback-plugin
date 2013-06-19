package org.jenkinsci.plugins.extremefeedback.XfManagementLink

def f=namespace(lib.FormTagLib)
def l=namespace(lib.LayoutTagLib)
def st=namespace("jelly:stapler")

l.layout() {
    l.header() {
        st.bind(var: "it", value: my)
        script(src: "/plugin/extreme-feedback/findlamps.js")
        link(rel: "stylesheet", type: "text/css", href:"/plugin/extreme-feedback/style.css")
    }
    l.main_panel() {
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
                    div(id: "button") {
                        button(onClick: "findlamps();", "Find lamps in the subnet")
                    }
                    div(id: "spinner", style: "display: none") {
                        text("loading...")
                    }
                }
                td {
                    div(id: "add-lamp") {
                        input(type: "text", id: "add-lamp-input", value: "IP Address")
                        button(onClick: "addLamp();", "Add Lamp")
                    }
                    div(id: "spinner2", style: "display: none") {
                        text("loading...")
                    }
                }
            }
        }

        div {
            button(onClick: "resetLamps();", "Reset Lamp List");
        }

        div {
            table(class:"sg-table", style: "text-align: left") {
                thead {
                    tr {
                        th {
                            text("MAC Address");
                        }
                        th {
                            text("IP Address");
                        }
                        th {
                            text("Name");
                        }
                        th {
                            text("Job(s) Assigned To")
                        }
                    }
                }
                tbody(id: "lamps-table") {
                }
            }
        }

        div {
            table(class: "sg-table", id: "lamp-job", style: "text-align: left") {
            }
        }

    }
}


