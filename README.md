GWT Tree
========

The GWT Tree Add-On demonstrates several interesting capabilities of the Add-On API. It shows using both dynamic trees (each set of
children are loaded only when the parent is expanded) and static trees (the entire tree is determined and loaded into the browser
at once). The static tree mode builds a sparse tree in the UI containing only the paths down to equipment that contain analog trend
sources. (If desired, the user can specify trend names to filter down to instead of all analog trends.) For the 24 hour period of
the day selected on the calendar, it then retrieves the trends from the selected (checked) equipments, integrates the values (which
may or may not make much sense for the given trend source) and shows a 3D bar graph (generated using JFreeChart) of the result.

The user interface for this Add-On is implemented using the [Google Web Toolkit](http://code.google.com/webtoolkit/). This is a
Java based tool for generating JavaScript enabled rich web applications. This is the tool enabling most of the UI, including the
tree and calendar. The tool makes it easy for someone with knowledge and experience developing Swing applications to develop web
applications. Some knowledge of HTML and CSS is still required, but little or no JavaScript knowledge is required.

The graphs shown in this Add-On are generated using [JFreeChart](http://www.jfree.org/jfreechart/), a free Java charting library.

Try it out
----------

Deploy the GWT Tree sample add-on by executing the 'deploy' task and starting (if necessary) the server. Note that this sample uses
analog trend sources, so you need a system with at least one such trend source and have some trend data for this sample to work
correctly.

Browse to `http://yourserver/gwttree`. This should present a login page. Log in with any valid operator and password.

After the tree appears on the bottom left, select (check) some equipment from the tree. The right side will then show all trend
sources in the selected equipment as tabs. Below the tab a graph of the calculated integration for the named trend in each checked
equipment will be displayed. Select a previous day on the calendar to change the graph. The "Change Tree Options..." button shows a
dialog allowing you to change between the dynamic tree (default) and the static tree. Futher, the dialog allows you to limit the
analog trend sources found by the add-on based on their name.

Important Lessons
-----------------

`GraphServlet.totalDemand` processes a span of trend data given a trend source and a date range. This uses the inner class
`GraphServlet.Integrator` as a `TrendProcessor`. to do this. This is a good example of a TrendProcessor performing a calculation
over a range of trend data with special handling for holes in the data.

`TreeServiceImpl` shows how to use a `TreeVisitor` to easily build a tree of objects that represents a sparse view of the original
tree. In this case, we are looking for equipment with analog trend sources. Any area that doesn't have a descendant that contains
such an equipment is not useful to show in the tree. This sample shows how about a dozen lines of code can prune these unnecessary
tree branches and create a tree of custom objects that can be remoted via GWT for use in rendering the user interface.