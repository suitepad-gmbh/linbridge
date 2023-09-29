# CHANGELOG
### 2.0.2
- FIX: set allowBackup to false
- FIX: Configure mk4 device for better audio

### 2.0.1
- FIX: linbridge crash
- FIX: receiver says busy after ringing for 1-2 seconds

### 2.0.0
- STYLE: added code style into the versioning
- FIX: linphone library is updated

### 1.5.4
- FEAT: add android 11 support

### 1.5.3
- FIX: NPE on invalid address

### 1.5.2
- CHORE: migrate to linphone android sdk instead of using our own compiled sdk
- FIX: outgoing calls now go through the proxy 

### 1.5.1
- FIX: crash reports not sent to crashlytics due to outdated firebase version
- FIX: crash when starting service without providing an action
- CHORE: migrate to androidx

### 1.5.0
- FEAT: add support for sip_auth_id if available

### 1.4.1
- CHORE: revert to old ringtone if linbridge will ring

### 1.4.0
- FEAT: can be configured to stop ringing by client app \
this is just a migration step and linphone should never be responsible ringing

### 1.3.3.rc2
- FEAT: get muting state

### 1.3.3.rc1
- CHORE: upgrade binbridge-api v1.0.1
  - FEAT: add G722 audio codec

### 1.3.2
- FIX: disable AVPF by default

### 1.3.1
- FEAT: rebuild using new google firebase api key

### 1.3.0
- FEAT: make log mails more browser friendly
- FEAT: add dialog for info input and confirmation before sending logs

### 1.2.0
- FEAT: send logs via email

### 1.1.0
- FEAT: add crashlytics configuration

### 1.0.3
- FEAT: collapse repeating log lines in a single one
- FEAT: show counter indicator on collapsed lines
- FIX: concurrent modification exception on log list

### 1.0.2
- FEAT: add firebase integration

### 1.0.1
- FIX: support min sdk 16

### 1.0.0.rc4/1.0.0
- CHORE: set java memory params for gradle build
- FIX: fail gracefully and logout when host is malformed

### 1.0.0.rc3
- disconnect from SIP server when providing null credentials instead of crashing

### 1.0.0.rc2
- add logging screen

### 1.0.0.rc1
- initial release
