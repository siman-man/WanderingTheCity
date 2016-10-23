Problem: WanderingTheCity

Problem Statement
問題文
The City covers the surface of a torus-shaped planet. It has only two types of buildings: black and white. All buildings of the same color are identical.
この町はトーラス形の宇宙で覆われています。そして黒と白の2つの種類の建物しかありません。全ての同じ色をした建物は同一です。
The buildings are arranged in a square grid of SxS square blocks, each block contains exactly one building, and there are streets between each pair of horizontally or vertically adjacent blocks.
建物はこのS x Sで囲われたグリッド空間上に配置されています。各ブロックにはかならず1つの建物が含まれています。また

You find yourself at one of the crossroads of the city. You don't know which crossroads it is. Fortunately, you have a map of the City, and you can walk around looking at the buildings.
あなたは自分だけの道の配置を見つけて下さい。交差点がどんなものかは知りません。幸運なことにあなたは町の地図を持っています。そしてあなたは辺りを歩いて建物を見ることが出来ます。
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
look() allows you to look around you when you're standing at a crossroads. You'll see four buildings immediately adjacent to this crossroads, which will be returned to you as a vector <string> with 2 rows and 2 columns. The orientation of this piece is the same as of the big map you're given initially. This method returns you the actual colors of the buildings, which might not match their colors on the map you're given!
walk(vector <int> shift) allows you to walk to a different crossroads. shift is a vector <int> with exactly two elements, shift[0] and shift[1] are distances you want to walk along vertical and horizontal axis, respectively. Both elements of shift must be between -S+1 and S-1, inclusive (remember that the map wraps around both horizontally and vertically). This method will return -1 if the call was invalid (provided invalid parameters or involved too much walking) and 0 in normal case.
guess(vector <int> coord) allows you to make a guess about the coordinates of the crossroads at which you've started your journey on the map. coord is a vector <int> with exactly two elements, coord[0] and coord[1] are coordinates of your guess along vertical and horizontal axis, respectively. Both elements of coord must be between 0 and S-1, inclusive. This method returns 1 if your guess is correct, 0 if it's incorrest, or -1 if the call was invalid (coordinates out of range or too many guesses).
A crossroads with coordinates (R, C) is located in the top left corner of the building in row R and column C. Thus, crossroads with coordinates (0, 0) is the top left corner of the map.

Your solution is allowed to make at most S^2 calls to each of look() and guess() functions, and to walk a Manhattan distance of at most 16*S^2 blocks in walk() functions.
