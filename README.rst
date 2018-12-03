Castle Elasticsearch Plugins
============================


Build
-----

  mvn package


Install
-------

Build:

  sudo /usr/share/elasticsearch/bin/plugin remove elasticsearch-castle-script

  sudo /usr/share/elasticsearch/bin/plugin install <download url>

  sudo /etc/init.d/elasticsearch restart



Run local
---------

With docker:

    docker build . -t ces2
    docker run -p 9200:9200 ces2


