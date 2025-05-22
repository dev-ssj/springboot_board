## 📚Spring Boot 기반 게시판
* 개발기간 : 2025.04.20 ~ 
* 참여인원 : 1명(개인 프로젝트)

## 프로젝트 개요
Spring Boot와 JPA를 개인적으로 공부하며 만든 학습용 게시판이다.</br>
기본적인 게시글 CRUD와 AWS S3를 이용한 이미지 업로드 및 삭제 기능을 구현하였으며, Azure의 MySQL과 연동하여 클라우드 환경에서 데이터를 관리했다.<br>
회원가입 및 로그인/로그아웃은 Spring security 기반으로 구현하였다.
* [Back End](https://github.com/dev-ssj/springboot_board/tree/master/src/main/java/com/example/board/)
* [Front End](https://github.com/dev-ssj/springboot_board/tree/master/src/main/resources/templates/)

## 구현 기능
* 게시판 CRUD
  + 게시글 목록 페이지
  + 게시글 상세 페이지
  + 게시글 등록
  + 게시글 수정
  + 게시글 삭제
* Spring Security 기반 회원가입
* 로그인/로그아웃
* AWS S3를 이용한 이미지 업로드 및 삭제
* Azure의 MySQL의 클라우드 서버와 연동

## 개발환경 및 사용언어
* OS : Windows 11
* DBMS : Azure MySQL
* IDE : IntelliJ IDEA 2023.2.8
* 클라우드 및 서비스 : AWS S3 
* Front-End : HTML5, CSS3, BootStrap4, JavaScript, Thymeleaf
* Back-End : JAVA, Spring Boot, JPA

## 시스템 설계
* ERD
![image](https://github.com/user-attachments/assets/4eefafa8-f32e-4496-ab86-ee0f74dc36f1)

## 구현화면
* 회원가입
![image](https://github.com/user-attachments/assets/442c4037-3cdc-4d0e-a437-a525e70a5e02)

* 로그인
![image](https://github.com/user-attachments/assets/52d51e74-8a25-41e8-9b0b-7b9a886cf938)

* 글 목록
* ![image](https://github.com/user-attachments/assets/2dac6e83-eb37-48ac-8d44-51fba29a838f)

* 글 상세
![image](https://github.com/user-attachments/assets/2a54a5d1-7c9c-4897-85f2-d3289edf513a)

* 글 작성
![image](https://github.com/user-attachments/assets/f58bf803-1a95-4d3d-ad84-48176adbfe33)

* 글 수정
![image](https://github.com/user-attachments/assets/e6be6691-4c3a-4533-b470-f85c78868e05)

## 트러블 슈팅
### [1]문제 발생
게시글 작성 시, 한번에 이미지를 첨부하는 것이 아닌 여러번에 걸쳐 첨부하는 경우, 이전에 선택한 이미지가 사라지고 새로 선택한 이미지로 덮어씌워지는 문제가 발생했다.

### [2]문제 원인
이 문제는 <input type="file"> 요소에서 기본적으로 제공되는 FileList 객체의 특성 때문에 발생한다.<br>
FlieList는 읽기 전용(read-only) 속성을 가지므로, 기존 파일 목록에 새 파일 목록을 병합하는 코드를 작성하더라도 수정이 불가했다.

### [3]문제 해결
이러한 문제를 해결하기 위해 JavaScript가 제공하는 `DataTransfer` 객체를 사용했다.<br>
DataTransfer는 파일을 드래그 앤 드롭하거나 복사할 때 사용되는 API로, 새로운 FlieList를 생성할 수 있다.<br>
사용자가 새 이미지를 첨부할 때마다 DataTransfer를 통해 기존 파일과 새 파일을 병합한 뒤 새로운 FileList로 변환하여 input 요소에 재할당하는 방식을 구현했다.<br>
Datatransfer을 활용한 덕분에 여러번에 걸친 이미지 첨부 시에도 이전 이미지가 유지되고, 새로운 이미지가 정상적으로 추가되도록 개선할 수 있었다.<br>

### [4]주요 코드 설명
```javascript
  const dataTransfer = new DataTransfer();  //DataTransfer객체 생성

//파일이 첨부 될때마다 실행
  $("#files").change(function () {
    const input = document.getElementById("files");
    const fileArr = input.files;  //사용자가 선택한 FileList 객체

      //파일 갯수만큼 for문 시행
      for (let i = 0; i < fileArr.length; i++) {
        if (!validation(fileArr[i])) {
          continue; // 유효성 검사에서 걸리면 해당 파일을 건너뛰고 계속 진행
        }
        const file = fileArr[i];
        dataTransfer.items.add(file); //선택한 파일들을 dataTransfer에 추가

        const reader = new FileReader();

      input.files = dataTransfer.files;  //dataTransfer에 저장된 파일들을 input 요소에 적용
    }

  //AJAX FORM 전송
  document.getElementById("form").addEventListener("submit", function (event) {
    event.preventDefault();

    const formData = new FormData();
    const title = document.getElementById("title").value.trim();
    const content = document.getElementById("content").value.trim();


    // dataTransfer.files에 들어간 파일의 갯수만큼 for문 진행
    for (let i = 0; i < dataTransfer.files.length; i++) {
      formData.append("files", dataTransfer.files[i]);  //dataTransfer.files에 담긴 실제 파일들을 서버로 전송
    }
```



