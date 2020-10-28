# NeoJukePro

![GitHub release (latest by date)](https://img.shields.io/github/v/release/nafu-at/NeoJukePro)
![GitHub](https://img.shields.io/github/license/nafu-at/NeoJukePro)
[![Build Status](https://travis-ci.com/nafu-at/NeoJukePro.svg?branch=master)](https://travis-ci.com/nafu-at/NeoJukePro)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/35ee3810e0b647de90d3251fad96bcc4)](https://www.codacy.com/gh/nafu-at/NeoJukePro/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nafu-at/NeoJukePro&amp;utm_campaign=Badge_Grade)

NeoJukeProはSimpleJukeの正統な後継として開発された新しいDiscord Music Botです。  
より単純明快で高機能かつ安定した動作ができるよう1から作り直しました。  

## 動作要項
- [x] Windows, MacOS, Linuxなどの一般的なPC用OS
- [x] Java11以降の実行環境
- [x] MariaDB 5.5以降 若しくは MySQL 5.5以降
- [x] Discordアカウント
    + [x] Presence Intent と Server Members Intentを有効にする必要があります。

### プログラムの不具合を発見した場合
当リポジトリにIssueを立てて報告してください。  
バグ修正・機能追加などのプルリクエストも歓迎しています。  
ソースコードを改変する際はオリジナルのコードスタイルを変更しないよう注意してください。

### ライセンス
このプログラムのオリジナルソースコードはApache License 2.0に基づき公開しています。

        Copyright 2020 NAFU_at.
    
       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

---

### Thaad Party License
#### MIT License
- SLF4J api
- Lavalink-Client

#### The 3-Clause BSD License
- Sentry Java For Logback

#### GNU General Public License, version 2
**(The Universal FOSS Exception, Version 1.0)**
- MySQL Connector/J

#### GNU Lesser General Public License 2.1
- Logback
- MariaDB Connector/J

#### Apache Lisence, Version 2.0
- JDA
- lavaplayer
- OkHttp3
- Jackson Core
- Jackson Databind
- jackson-dataformat-yaml
- Apache Commons Codec
- Apache Commons IO
- Apache Commons Lang
- HikariCP
- SQLite JDBC
- ASCII Table
