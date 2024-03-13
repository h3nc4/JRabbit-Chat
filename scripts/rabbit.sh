docker run -d --hostname chat --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3

# to stop the container use docker stop rabbit, same goes for 'start', 'remove', 'restart' etc.
