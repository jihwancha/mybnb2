# Kubectl install
<pre>
sudo apt-get update && sudo apt-get install -y apt-transport-https
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl
</pre>

# AWS-Cli v2 install
<pre>
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
(unzip이 없을 경우, 설치) sudo apt-get install unzip
sudo ./aws/install
</pre>

# AWS Configure
<pre>
aws configure
</pre>
- Access Key ID : 
- Secret Access Key : 
- Region : ap-northeast-2 (리전입력)
- Default output format : json

# EKS Client (eksctl) 설치
<pre>
curl --location "https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
</pre>

# EKS Cluster create
<pre>
eksctl create cluster --name (클러스터명) --version 1.15 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 4
</pre>

# EKS Cluster settings
<pre>
aws eks --region ap-northeast-2 update-kubeconfig --name (클러스터명)
kubectl config current-context
kubectl get all
</pre>

# ECR 인증
- Dockerhub 사용하는 경우 진행할 필요 없음
<pre>
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin (Account-ID).dkr.ecr.ap-northeast-2.amazonaws.com
</pre>
- 오류(unknown flag: --password-stdin) 발생 시,
<pre>
docker login --username AWS -p $(aws ecr get-login-password --region ap-northeast-2) (Account-ID).dkr.ecr.ap-northeast-2.amazonaws.com/
</pre>

# EKS Cluster delete
<pre>
eksctl delete cluster --name (클러스터명)
</pre>

# Metric Server 설치
<pre>
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml
kubectl get deployment metrics-server -n kube-system
</pre>

# Kafka install (kubernetes/helm)
참고 - (https://workflowy.com/s/msa/27a0ioMCzlpV04Ib#/a7018fb8c62)

<pre>
curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get | bash
kubectl --namespace kube-system create sa tiller      
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm init --service-account tiller
kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'

helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
helm repo update

helm install --name my-kafka --namespace kafka incubator/kafka
</pre>

# Kafka delete
<pre>
helm del my-kafka  --purge
</pre>

# Lab. Istio 설치
<pre>
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.4.5 sh -
cd istio-1.4.5
export PATH=$PWD/bin:$PATH
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
kubectl apply -f install/kubernetes/istio-demo.yaml

kubectl get pod -n istio-system
</pre>

# kiali 설치 

* 에러발생, 위의 istio 설치에서 완료됨
<pre>
vi kiali.yaml    

apiVersion: v1
kind: Secret
metadata:
  name: kiali
  namespace: istio-system
  labels:
    app: kiali
type: Opaque
data:
  username: YWRtaW4=
  passphrase: YWRtaW4=

----- save (:wq)

kubectl apply -f kiali.yaml
helm template --set kiali.enabled=true install/kubernetes/helm/istio --name istio --namespace istio-system > kiali_istio.yaml    
kubectl apply -f kiali_istio.yaml
</pre>

* load balancer로 변경
<pre>
kubectl edit service/kiali -n istio-system
(ClusterIP -> LoadBalancer)
</pre>

# namespace create
<pre>
kubectl create namespace mybnb
</pre>
* istio enabled
<pre>
kubectl label namespace mybnb istio-injection=enabled
</pre>

# siege deploy
<pre>
cd mybnb/yaml
kubectl apply -f siege.yaml 
kubectl exec -it siege -n mybnb -- /bin/bash
apt-get update
apt-get install httpie
</pre>

# ECR image repository 
<pre>
aws ecr create-repository --repository-name mybnb-gateway --region ap-northeast-2
</pre>

# image build & push

- compile
<pre>
cd mybnb/gateway
mvn package
</pre>
- for aws ecr
<pre>
docker build -t [AWS_ACCOUNT_ID].dkr.ecr.ap-northeast-2.amazonaws.com/mybnb-gateway:latest .
docker push [AWS_ACCOUNT_ID].dkr.ecr.ap-northeast-2.amazonaws.com/mybnb-gateway:latest

...
</pre>
- for dockerhub
<pre>
docker build -t jihwancha/mybnb-html:latest .
docker push jihwancha/mybnb-html:latest

...
</pre>

# application deploy
<pre>
cd mybnb/yaml

kubectl apply -f configmap.yaml

kubectl apply -f gateway.yaml
kubectl apply -f html.yaml
kubectl apply -f room.yaml
kubectl apply -f booking.yaml
kubectl apply -f pay.yaml
kubectl apply -f mypage.yaml

kubectl apply -f alarm.yaml
kubectl apply -f review.yaml
</pre>

# 숙소 등록 (siege 에서)
<pre>
http POST http://room:8080/rooms name=호텔 price=1000 address=서울 host=Superman
http POST http://room:8080/rooms name=펜션 price=1000 address=양평 host=Superman
http POST http://room:8080/rooms name=민박 price=1000 address=강릉 host=Superman
</pre>

# 예약 (siege 에서)
<pre>
http POST http://booking:8080/bookings roomId=1 name=호텔 price=1000 address=서울 host=Superman guest=배트맨 usedate=20201010
</pre>

# 예약 부하 발생 (siege 에서)
<pre>
siege -v -c100 -t60S -r10 --content-type "application/json" 'http://booking:8080/bookings POST {"roomId":1, "name":"호텔", "price":1000, "address":"서울", "host":"Superman", "guest":"배트맨", "usedate":"20201230"}'
</pre>

# kiali 확인
* 접속정보 : ip
<pre>
http://external-ip:20001 
(admin/admin)
</pre>

# auto scale out settings
<pre>
kubectl autoscale deploy booking -n mybnb --min=1 --max=10 --cpu-percent=15
kubectl get deploy booking -n mybnb -w
</pre>

# auto scale out delete
<pre>
kubectl delete hpa booking -n mybnb
</pre>

#  부하 발생 (siege 에서)
<pre>
siege -v -c100 -t60S -r10 --content-type "application/json" 'http://booking:8080/bookings'
</pre>







