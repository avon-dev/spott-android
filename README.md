포포 Phopo
===========
#### 팀프로젝트 (2019.11 ~ 2020.3)
> 인생샷 포인트 공유 안드로이드 어플<br/>

><p>
>  <img width="180" src="https://user-images.githubusercontent.com/51042849/77408893-c6568800-6dfb-11ea-9852-1e6808c64e28.jpg">  
 > <img width="180" src="https://user-images.githubusercontent.com/51042849/77408914-cf475980-6dfb-11ea-94eb-e7a4003ae265.jpg">
 > <img width="180" src="https://user-images.githubusercontent.com/51042849/77408931-d79f9480-6dfb-11ea-9686-a5d19cfce0b4.png">
>  <img width="180" src="https://user-images.githubusercontent.com/51042849/77408958-dec6a280-6dfb-11ea-99ac-ca04e076537c.jpg">
></p>

>>1. 인생샷 장소를 알 수 있어요.
다른 사람의 인생샷 어디서 찍었는 지 궁금하셨나요?
Phopo에서는 멋진 사진의 정확한 위치를 알 수 있습니다.
여행지 혹은 내 주변에서 인생샷 장소를 찾아보세요.<br/><br/>
>>2. 누구나 쉽게 인생샷을 찍을 수 있어요.
맘에 드는 사진을 발견하셨나요? 따라 찍어보세요!
윤곽선과 가이드사진을 이용하여 쉽게 구도를 잡을 수 있습니다.
누구나 인생샷을 남길 수 있어요!<br/><br/>
>>3. 인생샷 장소를 다른 사람들과 공유해보세요.
인생샷을 찍으셨나요?
다른 사람들과 멋진 사진을 나눠보세요!
내가 발견한 새로운 장소, 나만의 포즈를 다른 사람들에게 공유하세요.

<br/>

다운로드
-------
[<img width="180" src="https://user-images.githubusercontent.com/51042849/77410368-1c2c2f80-6dfe-11ea-856f-0ea48b2e3851.png">](https://play.google.com/store/apps/details?id=com.avon.spott)
<br/>

사용기술
-------
- 언어 : Kotlin
- 라이브러리 : GoogleMap, CameraX, OpenCV 
- API : Facebook Login
- 아키텍처 : MVP  

<br/>

기능
-------
### 1. 혼자 해결 팁 보기
<p>
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391841-14a85e80-6ddd-11ea-962e-bf942f406bf4.jpg">  
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391856-1bcf6c80-6ddd-11ea-9c36-a50d14e6c86f.jpg">
</p>

- 홈에서 '혼자 해결 팁 보기'를 누르면 혼자 수리를 시도해볼 수 있는 유튜브 영상을 볼 수 있습니다. 

### 2. 기사님 호출 및 채팅 문의
<p>
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391894-2ee23c80-6ddd-11ea-85b3-fff279794460.jpg">  
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391905-34d81d80-6ddd-11ea-86e6-804b9c6eb58f.jpg">
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391918-399cd180-6ddd-11ea-92e5-8f500349291a.jpg">
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391926-3e618580-6ddd-11ea-8528-ae4ae1b9e8fe.jpg">
</p>

- 홈에서 '당장 기사님 호출하기'를 누른 뒤, 고장 상황에 맞는 조건을 고르면 현재 출동이 가능한 기사님 목록을 조회할 수 있습니다.
- 기사님을 선택하면 기사님의 프로필, 리뷰, 가격, 설명 등을 볼 수 있습니다.
- 기사님에게 채팅 문의를 할 수 있습니다. (Firebase Realtime Database 활용)


### 3. 호출 내역 조회 및 작성한 리뷰ㆍ평점 관리
<p>
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391944-46212a00-6ddd-11ea-98c3-82c425545cb2.jpg">  
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77391954-4c170b00-6ddd-11ea-8328-e611a215f5db.jpg">
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392010-605b0800-6ddd-11ea-9a18-e56ce67e5096.jpg">
</p>

- 마이페이지에서 호출 내역 및 작성한 리뷰ㆍ평점을 조회 할 수 있습니다.
- 기사님이 작업완료한 호출에 대해 리뷰를 작성할 수 있고, 리뷰ㆍ평점 관리에서 수정, 삭제 할 수 있습니다.

<br/>

기사님 버전 기능
-------
### 1. 호출 조회 및 채팅 문의 답변
<p>
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392106-94362d80-6ddd-11ea-93a8-554acd07cf3a.jpg">  
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392118-99937800-6ddd-11ea-99cf-ec5935cebde4.jpg">
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392134-a021ef80-6ddd-11ea-8463-24aeb6ad7699.jpg">
</p>

- 홈에서 '출근하기'버튼을 누르면 일반 사용자가 해당 기사님을 호출할 수 있습니다.
- 기사님은 받은 호출을 거절ㆍ수락할 수 있고, 수락한 호출을 완료ㆍ취소 처리할 수 있습니다.

### 2. 작업 내역 조회 및 리뷰ㆍ평점 조회
<p>
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392158-a748fd80-6ddd-11ea-8655-c096cdd44ddd.jpg">  
  <img width="180" src="https://user-images.githubusercontent.com/51042849/77392190-b6c84680-6ddd-11ea-9cf3-8de7793ae4c7.jpg">
</p>

- 마이페이지에서 작업 내역과 리뷰ㆍ평점을 조회할 수 있습니다. 
