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


Then run:

    docker run -p 9200:9200 ces2


Copy java package file out:

    docker cp <docker id>:/opt/elasticsearch-castle-scoring/target/releases/elasticsearch-castle-script-2.3.5.zip elasticsearch-castle-script-2.3.5.zip