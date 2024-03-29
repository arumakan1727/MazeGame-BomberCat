# MazeGame-BomberCat

https://user-images.githubusercontent.com/33191176/154306193-273e4994-9abc-4b2b-a2bc-c60b6ec1f1ed.mp4

## ゲームの概要
本ゲームはJavaで書かれたPC用ゲームです。

- 暗闇の迷路で猫を操作して、ゴールするのが目標です。
- マップ上には鍵が3つ存在します。ゴールするには鍵を全て拾う必要があります。
- 猫は **爆弾** を置いて壁を壊すことができます。
- 一定時間の経過でアイテムが使用できます。

このゲームは大学の某プログラミング授業(2020年度) で作成して投票数1位を獲得しました

- コインや鍵を拾うとスコアを得られます。スコアに特に意味はありません。
- 制限時間はありません。早くゴールしても特典はありません。
- 迷路はプレイするたびにランダムに生成されます。

## 操作方法
### 猫の移動
カーソルキーまたは Vim でおなじみの h, j, k, l キーで猫を移動できます。
キーの長押しでその方向へ移動し続けます。

### 爆弾
爆弾はスペースキーを押すことで自分のいるマスに設置できます。
爆弾および爆風に触れてもペナルティはありません。
爆弾は設置3秒後に爆発して、画像のように十字の領域の壁ブロックを破壊します。

![image](https://user-images.githubusercontent.com/33191176/154324500-0674ba91-3d67-4f63-9fb5-aca6f95434c7.png)

画面上に同時に存在できる爆弾の個数は3個です。

### アイテム
画面上部には3つのアイテムのボタンがあります。
左から順に、①コインの小道 ②コインの杖 ③フィーバースター です。

![image](https://user-images.githubusercontent.com/33191176/154324593-8a9d5c16-fea4-45d9-801c-c3e931a075ef.png)

一定時間経過するとボタンのゲージが溜まり、そのアイテムを発動できるようになります。
アイテムを発動する方法は以下の2つの好きな方を使用できます。

- ボタンをマウスでクリックする
- 数字の 1, 2, 3 キーを押す (1キーなら ①コインの小道、3キーなら ③フィーバースターを発動)

発動したらゲージが空になり、再び溜まっていきます。

#### 【アイテム1:  コインの小道】
自分のいるマスからゴールまでの最短経路にコインが出現します。
(詳細は冒頭のデモ動画を参照)

#### 【アイテム2: コインの杖】
自分を中心に 5x5 マスにあるコインを一括でゲットします。
(詳細は冒頭のデモ動画を参照)

#### 【アイテム3: フィーバースター】
一定時間フィーバーモードになり、以下の効果を得られます。

- 暗闇が一時的に晴れる
- ボムが強化される (ゴールドボム)
    - 広範囲を爆破
    - 爆破したブロックをコインにする
- スコアが 1.5 倍
- 移動速度UP


## 実行に必要な要件
- Java 11 以上
- 「JavaFX ライブラリがインストールされている」または「Liberica JDK がインストールされている」
    - JavaFX: https://openjfx.io/
    - Liberica JDK (JavaFX を同梱している JDK): SDKMAN でのインストールがおすすめです: https://sdkman.io/
        - SDKMAN でインストールする場合、例えば `sdk install java 11.0.14-librca` のように実行すればよいです。


## コンパイル・実行方法
2つの方法があります。
作者としては Liberica JDK のほうが簡単で確実だと思います。

### 方法1: Liberica JDK でビルド・実行する
まず、 `javac` および `java` コマンドが Liberica のものになっていることを確認してください。

プロジェクトのトップディレクトリ (`launch-for-liberica.sh` があるディレクトリ) で、以下のコマンドを実行すれば起動できます。

```
./launch-for-liberica.sh
```


### 方法2: JavaFX ライブラリをリンクしてビルド・実行する

プロジェクトのトップディレクトリ (`Makefile` があるディレクトリ) で、以下のコマンドを実行します。
(`$` 記号は不要)

```
make JFX_LIB='path/to/openjfx-lib' run
```

JFX_LIB を指定しない場合は、デフォルト値として `/usr/share/openjfx/lib` を使用します。

例:
```
make JFX_LIB=~/.local/lib/javafx-sdk-11.0.2/lib run
```


## 謝辞・素材提供
下記サイトのフリー画像素材を使わせていただきました。

- https://opengameart.org
- https://www.flaticon.com
- https://www.civillink.net

音源素材は下記サイトのものを使わせていただきました。

- https://opengameart.org
- https://www.music-note.jp

この場を借りてお礼申し上げます。  
I would like to take this opportunity to thank you.
