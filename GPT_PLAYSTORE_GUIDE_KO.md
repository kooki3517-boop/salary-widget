# 플레이스토어 배포 준비 안내

## 현재 준비된 것
- 기존 테스트용 APK 빌드 유지
- 플레이스토어용 AAB 빌드 추가
- 버전 번호 12 / 버전명 2.7
- GitHub Actions에서 비밀 키를 이용한 릴리스 서명 지원

## 네가 해야 할 일
1. Google Play Console 개발자 계정 개설 (등록비 및 신원 확인 등)
2. 출시용 고유 패키지명 확정. 현재는 `com.example.salarygrows` 임시값으로, 첫 스토어 출시 전 변경 권장. 패키지명을 바꾸면 기존 직접 설치한 앱과 별개 앱으로 취급될 수 있음.
3. 안전한 업로드 서명키 생성 및 별도 보관. 키 파일을 GitHub 소스에 올리지 말 것.
4. GitHub 저장소 Settings > Secrets and variables > Actions에 다음 등록:
   - ANDROID_KEYSTORE_BASE64: 서명키 파일을 Base64로 인코딩한 내용
   - ANDROID_KEYSTORE_PASSWORD: 키스토어 암호
   - ANDROID_KEY_ALIAS: 키 별칭
   - ANDROID_KEY_PASSWORD: 키 암호
5. GitHub Actions에서 `salary-grows-play-aab` 다운로드. 서명 비밀값이 없으면 AAB가 만들어져도 플레이스토어에 업로드할 수 없음.
6. Play Console에서 앱 설명, 스크린샷, 개인정보 처리방침/데이터 보안 등 필수 항목 작성, 요구되는 테스트 및 심사 진행.
7. 승인된 버전이 게시되면 플레이스토어 자동 업데이트 설정을 켠 사용자에게 업데이트가 배포될 수 있음.

## 유의
- GitHub에 소스를 푸시하는 것만으로 스토어에 자동 출시되지는 않음.
- 정식 출시 후에는 `versionCode`를 새 버전마다 증가시켜야 함.
- 이 파일은 빌드/배포 준비용 소스이며 실제 빌드 및 플레이스토어 심사는 검증되지 않음.
