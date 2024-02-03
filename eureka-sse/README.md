# Eureka Semi-SSE

[위로 가기](../README.md)

MSA 환경에서 마이크로 서비스를 배포할 경우 registry를 가져오는 시간으로 인해 지연이 생깁니다.

스프링 클라우드 Eureka의 경우 eureka client는 registry를 폴링 방식으로 갱신합니다.

이 방식을 인스턴스가 등록되었을 때 클라이언트가 registry를 갱신하도록 변경한 구조로 만들었습니다.

<img width="363" alt="image" src="https://github.com/progress0407/progress0407/assets/66164361/3d58a32f-2ac3-4349-a489-c132668d7cdc">
