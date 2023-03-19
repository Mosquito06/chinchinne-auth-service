## :closed_book: 친친네 가계부_Auth_Service(업데이트 중)

## :bulb: 개요

1. 시스템 구성도
<img src="https://user-images.githubusercontent.com/32257949/226171436-8bbc95b4-081a-48a6-b256-dd5288043cb8.jpeg"  width="750" height="370">
<img src="https://user-images.githubusercontent.com/32257949/226171446-79f0ebda-7b24-4a45-97f1-44e97ce3d4fe.jpeg"  width="750" height="370">

  * 모놀리식 아키텍처로 구현해도 무리없는 프로젝트이나 분산 아키텍처 개념 및 학습을 위해 (서비스 기반)분산 아키텍처로 구상하여 개발 진행 중
  * 모든 서비스는 이중화를 고려하여, 랜덤 포트를 사용하도록 구현
  * 모든 요청은 Gateway-Service를 통해 전달
  * (현재)서비스 도메인을 철저히 분리하여 서비스 간 호출을 고려하지 않음
  * (현재)단일 데이터 베이스 사용(MariaDB)
  * (계획)Apache Kafka를 사용하여 서비스에서 레코드를 발행, 데이터 베이스에서 소비 구조로 변경
  * (계획)CQRS 패턴을 구현하여 Query 요청은 MongoDB에서 조회
  * (계획)화면단 이벤트를 Elasticsearch에 저장 및 시각화 처리

2. 기술 스택 및 설명
<div align="left">
  <img src="https://img.shields.io/badge/Java-6DB33F?style=for-the-badge">
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Security OAuth2-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=Spring&logoColor=white">
</div>

  * 친친네에서 사용할 가계부 Auth 서비스
  * Authorization Code Grant Type, Refresh Token Grant Type으로 Token을 발행
  * Token 발행 내역은 데이터 베이스에 저장 및 관리

3. 구현(예정) 기능
  * JWT 발행 및 유효성 검증
  * 사용 기간이 만료된 Token 데이터를 주기적으로 삭제하는 Batch 작성 예정
