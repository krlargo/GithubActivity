docker ps -a > /tmp/yy_xx$$
if grep --quiet web1 /tmp/yy_xx$$
then
	docker run -d --name web2 --network ecs189_default $1
	docker exec ecs189_proxy_1 /bin/bash /bin/swap2.sh
	docker rm -f `docker ps -a | grep web1 | sed -e 's: .*$::'`
else
	docker run -d --name web1 --network ecs189_default $1
	docker exec ecs189_proxy_1 /bin/bash /bin/swap1.sh
	docker rm -f `docker ps -a | grep web2 | sed -e 's: .*$::'` 
fi
