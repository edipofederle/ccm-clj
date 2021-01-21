# Description
	
This project implements the the *Closeness Centrality* Metric. Centrality metrics try to approximate a measure of influence of an
inviduvual wihin a social network.

# The Core

Initially we have a file, where each line contains two values (vertex) separated by a single space. Representing an edge between those
two nodes (vertex).This file is located into ``resources`` directory. The ``profiles.clj`` file indicates which file should be used
considering the environment (development or test, in this case)


The first part consist in rank the vertices on the undirected graph by their *closeness*. To do this,first we read the file, and
turn it into a data structure like this: ``[[:1 :2] [:2 :3] [:3 :4]`` and convert it to ``{:1 {:2 1} :2 {:1 1 :3 1} 3 {:2 1 :4 1} :4 {:3 1}}``
that representing the edges. Given the edges we use Dijkstra algorithm to find shortest-paths, that will return us a data structure like this:

``
	{:1 {:shortest-paths {:1 0 :2 1 :3 2 :4 3}}
   :2 {:shortest-paths {:1 1 :2 0 :3 1 :4 2}}
   :3 {:shortest-paths {:1 2 :2 1 :3 0 :4 1}}
   :4 {:shortest-paths {:1 3 :2 2 :3 1 :4 0}}
	 ``

With this data structure in hand we build the customersy, each customer is represented as:

``{:1 {:shortest-paths {:1 0, :2 1, :3 2}, :fraudulent false, :score 1/2}}``

Finally we sort (descending order) all customers by their score (centrality). We have other two main functions
responsible to add ne edges, and to mark a customer (node) as fraudulent. The first one, just receive a
vector o keywords (e.g: ``[:3 :5]``) and add to the list of customers, all customers shortest paths are updated with
the new edges. The second one receive a node name (e.g: ``1``) and mark it as fraudulent as such:

* The fraudulent customer score should be zero;
* Customers directly related to the fraudulent customer should have their score halved;
* More generally, scores of customers indirectly referred by the fraudulent customer should be multiplied by a coefficient F: ``F(k) = (1 - (1/2)^k)``.
where K is the lenght of the shortest path from the fraudulent customer to the customer in question.

# The API

The API has three main endpoints, for:

* Get all customers sorted by centrality;
* Add new edge;
* Mark a customer as fraudulent.


## How to use

``lein ring server`` to start the server


I use this Ruby GEM for format the output:

``gem install colorful_json``


## Get all customers sorted by centrality
``curl "http://localhost:8080/api/customer" | cjson | less``

You can use the ``less`` command to paginate the results.

## Add new edge
``curl -H "Content-Type: application/json" -X POST -d "{\"source\" : \"3\", \"destination\" : \"5\"}" "http://localhost:8080/api/edge" |cjson``

## Mark a customer as fraudulent
``curl -X PUT  http://localhost:8080/api/customer/2/fraudulent | cjson``



# Tests

``lein with-profile test midje``
