## :closed_book: 친친네 가계부_Auth_Service(업데이트 중)

## :bulb: 개요

1. 시스템 구성도
<img src="https://user-images.githubusercontent.com/32257949/233838423-f7f16d7a-cdb1-460d-83fa-ca221ff7280d.jpeg"  width="750" height="370">
<img src="https://user-images.githubusercontent.com/32257949/233838425-47434a10-b0f1-4d82-97ec-c7c45d2382e4.jpeg"  width="750" height="370">

  * 모놀리식 아키텍처로 구현해도 무리없는 프로젝트이나 분산 아키텍처 개념 및 학습을 위해 (서비스 기반)분산 아키텍처로 구상하여 개발 진행 중
  * 모든 서비스는 이중화를 고려하여, 랜덤 포트를 사용하도록 구현
  * 모든 요청은 Gateway-Service를 통해 전달
  * 서비스 도메인을 철저히 분리하여 서비스 간 호출을 고려하지 않음
  * Global Transaction으로 MariaDB, MongoDB에 데이터 동시 저장
  * Global Transaction 사용을 위해 MongoDB는 Replica Set으로 구현
  * (진행중) CQRS 패턴을 구현하여 Query 요청은 MongoDB에서 조회
  * (계획) 화면단 이벤트를 Apache Kafka를 통해 발행하고, Elasticsearch에 적재 및 시각화
  * (계획) Spring Actuator를 통해 서버 메트릭을 Elasticsearch에 적재 및 시각화
  * (계획) Pinpoint 세팅 및 Locust를 통한 부하테스트

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
