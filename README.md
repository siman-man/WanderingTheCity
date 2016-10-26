Problem: WanderingTheCity

Problem Statement
問題文
The City covers the surface of a torus-shaped planet. It has only two types of buildings: black and white. All buildings of the same color are identical.
この町はトーラス形の宇宙で覆われています。そして黒と白の2つの種類の建物しかありません。全ての同じ色をした建物は同一です。
The buildings are arranged in a square grid of SxS square blocks, each block contains exactly one building, and there are streets between each pair of horizontally or vertically adjacent blocks.
建物はこのS x Sで囲われたグリッド空間上に配置されています。各ブロックにはかならず1つの建物が含まれています。また、隣接したブロックの間には街路が存在しています

You find yourself at one of the crossroads of the city. You don't know which crossroads it is. Fortunately, you have a map of the City, and you can walk around looking at the buildings.
あなたある一つの交差点を探し出して下さい。交差点がどんなものかは知りません。幸運なことにあなたは町の地図を持っています。そしてあなたは辺りを歩いて建物を見ることが出来ます。
Unfortunately, the map you have is outdated; some of the buildings in the city have been painted a different color since it was printed, so the City you see differs from the map.
しかし残念なことに、地図は外部に保存されているのです。町の幾つかの建物は異なる色になっています。なのであなたの見た情報とマップの情報は異なります。

Your task is to figure out at which crossroads you started.
あなたの目的はあなたの開始地点となる交差点をみつけることです。

Implementation Details
Your code must implement one method whereAmI(vector <string> map, int W, int L, int G).
あなたは whereAmI を実装する必要があります
This method will be called once; it will give you the (outdated) map of the City as a vector <string>, with black and white buildings denoted as 'X' and '.', respectively,
このメソッドは1度だけ呼び出されます。そしてあなたに街の情報を渡します。白と黒の建物Xとxが繰り返された
and constants used in scoring (see Scoring section). The return value of this method is an integer, and it will be ignored during scoring. From this method you can call the following library functions of class Actions:
look() allows you to look around you when you're standing at a crossroads.
You'll see four buildings immediately adjacent to this crossroads, which will be returned to you as a vector <string> with 2 rows and 2 columns. The orientation of this piece is the same as of the big map you're given initially. This method returns you the actual colors of the buildings, which might not match their colors on the map you're given!
walk(vector <int> shift) allows you to walk to a different crossroads. shift is a vector <int> with exactly two elements, shift[0] and shift[1] are distances you want to walk along vertical and horizontal axis, respectively. Both elements of shift must be between -S+1 and S-1, inclusive (remember that the map wraps around both horizontally and vertically). This method will return -1 if the call was invalid (provided invalid parameters or involved too much walking) and 0 in normal case.

guess(vector <int> coord) allows you to make a guess about the coordinates of the crossroads at which you've started your journey on the map.
coord is a vector <int> with exactly two elements, coord[0] and coord[1] are coordinates of your guess along vertical and horizontal axis, respectively.
Both elements of coord must be between 0 and S-1, inclusive. This method returns 1 if your guess is correct, 0 if it's incorrest, or -1 if the call was invalid (coordinates out of range or too many guesses).
A crossroads with coordinates (R, C) is located in the top left corner of the building in row R and column C. Thus, crossroads with coordinates (0, 0) is the top left corner of the map.

Your solution is allowed to make at most S^2 calls to each of look() and guess() functions, and to walk a Manhattan distance of at most 16*S^2 blocks in walk() functions.


## 考察

ある一つの交差点を求める問題、交差点を求めるには外部に保存されている地図と、アクションによる行動によって推測する
いわゆるリアクティブ型の問題

  * 盤面の広さは50-500
  * 歩くコストは1-10
  * 周りを見渡すコストは盤面の広さ/2 - 盤面の広さ
  * 開始地点を予測するコストは盤面の広さ/2 - 2*盤面の広さ

  * 古い地図と新しい地図に違いが存在していた場合にどのような情報が得られるのか
  * 歩くのはある特定の地点を見るための作業
    * ある見渡したい地点を複数求めた時に最短のコストで移動する問題はありそう
  * どの地点を見れば自分の座標がわかるのか

  * もし古い地図ではなく新しい地図を最初から保持していた場合にはどうなるか
    * 町はある一定部分を繰り返すことで生成されているが、その繰り返しと異なる部分を見つけ出す
    * 見つけ出したそこまでの経路から逆算で求めることが可能

## 各アクションについて

  * 現時点の最新の情報を取得する
  * 歩く
  * スタート地点を予測する

## ビジュアライザコードリーディング

  * 街のちずには古いものと新しいものがある。
  * 最初に渡される地図は古い地図
  * 一部新しい地図と建物が異なる
  * y軸とx軸はループしている
  * 1%の確率で古い情報と異なる(ランダム)
  * 5-20%の確率で建物が反転している

## スタート地点を予測するのに必要な情報

  * 街はループしている
  * 街の縦幅と横幅がわかる
  * ある特定の座標がわかればそこから相対座標で割り出せる

## 解法

 * とりあえず全部の地点を予想してみる（必ず当たる）
   * と思ったけどTimeoutになって落ちた :) そんなに標準出力したとは思わないけど...
