#include <iostream>
#include <vector>
#include <algorithm>
#include <cassert>

using namespace std;

class WanderingTheCity {
public:
  int whereAmI(vector<string> cityMap, int W, int L, int G) {
  }
};

int main() {
  int S, W, L, G;
  cin >> S;
  vector<string> cityMap;
  for (int i = 0; i < S; i++) {
    string line;
    cin >> line;
    cityMap.push_back(line);
  }
  cin >> W;
  cin >> L;
  cin >> G;
  WanderingTheCity wtc;
  int ret = wtc.whereAmI(cityMap, W, L, G);
  cout << "!" << endl;
}
