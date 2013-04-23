package org.jenkinsci.plugins.extremefeedback.XfManagementLink

def f=namespace(lib.FormTagLib)
def l=namespace(lib.LayoutTagLib)
def st=namespace("jelly:stapler")

l.layout() {
    l.header() {
        st.bind(var: "it", value: my)
        script(src: "/plugin/extreme-feedback/findlamps.js")
    }
    l.main_panel() {
        h1 {
            text(my.displayName)
        }
        div {
            p {
                text("Order your lamps at ")
                a(href: "http://www.extremefeedback.com") {
                    text("extremefeedback.com")
                }
            }
        }

        div(id: "button") {
            button(onClick:"findlamps();", "Find Lamps")
        }
        div(id: "spinner", style: "display: none") {
            text("loading...")
        }

        div {
            table(class:"pane") {
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
            table(class: "pane", id: "lamp-job") {
            }
        }



    }
}


