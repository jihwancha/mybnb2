# 환경구성

## EKS Cluster create
<pre>
$ eksctl create cluster --name skccuser22-cluster --version 1.15 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 4
</pre>
* EKS Cluster settings
<pre>
$ aws eks --region ap-northeast-2 update-kubeconfig --name skccuser22-cluster
$ kubectl config current-context
$ kubectl get all
</pre>

* ECR 인증
<pre>
$ aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com
</pre>

* Metric Server 설치
<pre>
$ kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml
$ kubectl get deployment metrics-server -n kube-system
</pre>

* Kafka install (kubernetes/helm)
<pre>
$ curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get | bash
$ kubectl --namespace kube-system create sa tiller      
$ kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
$ helm init --service-account tiller
$ kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'
$ helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
$ helm repo update
$ helm install --name my-kafka --namespace kafka incubator/kafka
$ kubectl get all -n kafka
</pre>

* Istio 설치
<pre>
$ curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.4.5 sh -
$ cd istio-1.4.5
$ export PATH=$PWD/bin:$PATH
$ for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
$ kubectl apply -f install/kubernetes/istio-demo.yaml
$ kubectl get pod -n istio-system
</pre>

* Kiali 설정
<pre>
$ kubectl edit service/kiali -n istio-system
</pre>
- type 변경 : ClusterIP -> LoadBalancer
- (접속주소) http://aaa1acc40ce7e4255b8ce9098a68f927-1757157123.ap-northeast-2.elb.amazonaws.com:20001

* Namespace 생성
<pre>
$ kubectl create namespace mybnb
</pre>

* Namespace istio enabled
<pre>
$ kubectl label namespace mybnb istio-injection=enabled 
</pre>
- (설정해제 : kubectl label namespace mybnb istio-injection=disabled --overwrite)

# Build & Deploy

* ECR image repository
<pre>
$ aws ecr create-repository --repository-name skccuser22-gateway --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-html --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-room --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-booking --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-pay --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-mypage --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-review --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-alarm --region ap-northeast-2
$ aws ecr create-repository --repository-name skccuser22-auth --region ap-northeast-2
</pre>

* image build & push
<pre>
$ cd ~/jihwancha/mybnb2/gateway
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-gateway:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-gateway:latest

$ cd ~/jihwancha/mybnb2/html
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-html:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-html:latest

$ cd ~/jihwancha/mybnb2/room
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-room:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-room:latest

$ cd ~/jihwancha/mybnb2/booking
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-booking:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-booking:latest

$ cd ~/jihwancha/mybnb2/pay
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-pay:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-pay:latest

$ cd ~/jihwancha/mybnb2/mypage
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-mypage:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-mypage:latest

$ cd ~/jihwancha/mybnb2/review
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-review:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-review:latest

$ cd ~/jihwancha/mybnb2/alarm
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-alarm:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-alarm:latest

$ cd ~/jihwancha/mybnb2/auth
$ mvn clean package
$ docker build -t 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-auth:latest .
$ docker push 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/skccuser22-auth:latest
</pre>

* Deploy
</pre>
$ cd ~/jihwancha/mybnb2/yaml
kubectl apply -f siege.yaml
kubectl apply -f configmap.yaml
kubectl apply -f gateway.yaml
kubectl apply -f html.yaml
kubectl apply -f room.yaml
kubectl apply -f booking.yaml
kubectl apply -f pay.yaml
kubectl apply -f mypage.yaml
kubectl apply -f alarm.yaml
kubectl apply -f review.yaml
kubectl apply -f auth.yaml
</pre>

# 사전 검증

* 화면
- http://a2cd95eb5dfa547c290e7c877e07d62c-1371768300.ap-northeast-2.elb.amazonaws.com:8080/html/index.html

* siege 접속
<pre>
kubectl exec -it siege -n mybnb -- /bin/bash
apt-get update
apt-get install httpie
</pre>

* 숙소 등록 (siege 에서)
<pre>
http POST http://room:8080/rooms name=호텔 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션 price=1000 address=양평 host=Superman
http POST http://room:8080/rooms name=민박 price=1000 address=강릉 host=Superman
</pre>

* 숙소 확인 (siege 에서)
<pre>
http http://room:8080/rooms
</pre>

* 인증 확인 (siege 에서)
<pre>
http http://auth:8080/auths
</pre>

* 예약 (siege 에서)
<pre>
http POST http://booking:8080/bookings roomId=1 name=호텔 price=1000 address=서울 host=Superman guest=배트맨 usedate=20201010
</pre>

* 예약 확인 (siege 에서)
<pre>
http http://booking:8080/bookings/1
</pre>

# 검증1) 동기식 호출 과 Fallback 처리
* 숙소 등록시 인증 서비스를 동기식으로 호출하고 있음.
* 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 인증시스템이 장애가 나면 숙소 등록을 못받는다는 것을 확인

* 인증 서비스 중지
<pre>
$ kubectl delete -f auth.yaml
</pre>

* 숙소 등록 에러 확인 (siege 에서)
<pre>
http POST http://room:8080/rooms name=호텔1 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션1 price=1000 address=양평 host=Superman
</pre>

* 인증 서비스 기동
<pre>
$ kubectl apply -f auth.yaml
</pre>

* 숙소 등록 성공 확인 (siege 에서)
<pre>1
http POST http://room:8080/rooms name=호텔2 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션2 price=1000 address=양평 host=Superman
</pre>

# 검증2) 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

* 숙소가 등록된 후에 알림 처리는 동기식이 아니라 비동기식으로 처리하여 알림 시스템의 처리를 위하여 등록 블로킹 되지 않아도록 처리한다.

* 알림 서비스 중지
<pre>
kubectl delete -f alarm.yaml
</pre>

* 숙소 등록 성공 확인 (siege 에서)
<pre>
http POST http://room:8080/rooms name=호텔3 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션3 price=1000 address=양평 host=Superman
</pre>

* 알림 이력 조회 불가 확인 (siege 에서)
<pre>
http http://alarm:8080/alarms
</pre>

* 알림 서비스 기동
<pre>
kubectl apply -f alarm.yaml
</pre>

* 알림 이력 조회 성송 확인 (siege 에서)
<pre>
http http://alarm:8080/alarms 
</pre>

# 검증3) 동기식 호출 / 서킷 브레이킹 / 장애격리
* 서킷 브레이킹 프레임워크의 선택: istio-injection + DestinationRule
* 숙소 등록시 동기로 호출되는 인증 서비스에 설정

* istio-injection 적용 (기 적용완료)
<pre>
kubectl label namespace mybnb istio-injection=enabled
</pre>

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 60초 동안 실시
<pre>
$ siege -v -c10 -t60S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔4", "price":1000, "address":"서울", "host":"Superman"}'
</pre>

* 서킷 브레이킹을 위한 DestinationRule 적용
<pre>
$ cd ~/jihwancha/mybnb2/yaml
$ kubectl apply -f dr-auth.yaml
</pre>

* 서킷 브레이킹 확인 (kiali 화면)

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 60초 동안 실시
<pre>
$ siege -v -c10 -t60S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔5", "price":1000, "address":"서울", "host":"Superman"}'
</pre>

* 서킷 브레이킹을 위한 DestinationRule 제거
<pre>
$ cd ~/jihwancha/mybnb2/yaml
$ kubectl delete -f dr-auth.yaml
</pre>

* 정상 동작 확인


# 검증4) 오토스케일 아웃
* 인증 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 3개까지 늘려준다:

* HPA 적용
<pre>
$ kubectl autoscale deploy auth -n mybnb --min=1 --max=3 --cpu-percent=15
$ kubectl get deploy auth -n mybnb -w 
</pre>

* 숙소등록 부하 발생 (siege 에서) - 동시사용자 10명, 180초 동안 실시
<pre>
$ siege -v -c10 -t180S -r10 --content-type "application/json" 'http://room:8080/rooms POST {"name":"호텔6", "price":1000, "address":"서울", "host":"Superman"}'
</pre>

* 스케일 아웃 확인


# 검증5) 무정지 재배포

* 리뷰조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
<pre>
siege -v -c1 -t300S -r10 --content-type "application/json" 'http://review:8080/reviews'
</pre>

* 리뷰 이미지 update (readness, liveness 미설정 상태)
<pre>
$ kubectl apply -f review_na.yaml 실행
</pre>

* 에러 발생 확인 (siege 에서)


* 리뷰조회 부하 발생 (siege 에서) - 동시사용자 1명, 300초 동안 실시
<pre>
siege -v -c1 -t300S -r10 --content-type "application/json" 'http://review:8080/reviews'
</pre>

* 리뷰 이미지 update (readness, liveness 설정 상태)
<pre>
$ kubectl apply -f review.yaml 실행
</pre>

* 정상 실행 확인 (siege 에서)



