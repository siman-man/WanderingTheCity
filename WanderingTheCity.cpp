#include <iostream>
#include <vector>
#include <algorithm>
#include <cassert>
#include <string.h>

using namespace std;

int S;

const int FAIL = 0;
const int HIT = 1;

class WanderingTheCity {
  public:
    int whereAmI(vector<string> cityMap, int W, int L, int G) {
      S = cityMap.size();
      Actions ac;

      fprintf(stderr,"S = %d\n", S);

      for (int y = 0; y < S; y++) {
        for (int x = 0; x < S; x++) {
          vector<int> coord;
          coord.push_back(y);
          coord.push_back(x);
          int result = ac.guess(coord);
          if (result == HIT) return 0;
        }
      }
    }
};

class Actions {
  public:
    vector<string> look() {
      cout << "?look" << endl;
      vector<string> ret(2);
      cin >> ret[0];
      cin >> ret[1];

      return ret;
    }

    int walk(vector<int> shift) {
      cout << "?walk" << endl;
      cout << shift[0] << endl;
      cout << shift[1] << endl;

      int result;
      cin >> result;
      return result;
    }

    int guess(vector<int> coord) {
      cout << "?guess" <<endl;
      cout << coord[0] << endl;
      cout << coord[1] << endl;

      int result;
      cin >> result;
      return result;
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
