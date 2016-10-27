#include <iostream>
#include <vector>
#include <algorithm>
#include <cassert>
#include <string.h>

using namespace std;

int S;

const int HIT = 1;

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

vector<string> g_oldCityMap;
vector<string> g_cityMap;
vector<string> g_myMap;

class WanderingTheCity {
  public:
    Actions ac;
    int posY;
    int posX;
    int currentCost;
    int walkCost;
    int lookCost;
    int guessCost;

    void init(vector<string> cityMap, int W, int L, int G) {
      S = cityMap.size();
      g_oldCityMap = cityMap;
      g_cityMap = cityMap;
      g_myMap = cityMap;
      walkCost = W;
      lookCost = L;
      guessCost = G;
      currentCost = 0;

      for (int y = 0; y < S; y++) {
        for (int x = 0; x < S; x++) {
          g_myMap[y][x] = '-';
        }
      }

      posY = 0;
      posX = 0;

      fprintf(stderr,"S = %d\n", S);
    }

    int whereAmI(vector<string> cityMap, int W, int L, int G) {
      init(cityMap, W, L, G);

      fprintf(stderr,"S = %d\n", S);

      walkAllMap();

      showOldCityMap();
      showMyMap();
      showAround();

      for (int y = 0; y < S; y++) {
        for (int x = 0; x < S; x++) {
          int result = guess(y, x);
          if (result == HIT) return 0;
        }
      }
    }

    void walkAllMap() {
      for (int y = 0; y < S; y += 2) {
        for (int x = 0; x < S; x += 2) {
          look();
          walk(0, 2);
        }
        walk(2, 0);
      }
    }

    int applyShift(int cur, int shift) {
      return (cur + shift + S) % S;
    }

    /**
     * walk map
     */
    void walk(int shiftY, int shiftX) {
      int ny = applyShift(posY, shiftY);
      int nx = applyShift(posX, shiftX);
      vector<int> shifts(2);
      shifts[0] = ny;
      shifts[1] = nx;

      fprintf(stderr,"move... (%d, %d) => (%d, %d)\n", posY, posX, ny, nx);
      ac.walk(shifts);
      posY = ny;
      posX = nx;
      assert(0 <= posY && posY <= S);
      assert(0 <= posX && posX <= S);
    }

    /**
     * Look around city building.
     */
    void look() {
      vector<string> view = ac.look();

      for (int y = 0; y < 2; y++) {
        fprintf(stderr,"%s\n", view[y].c_str());
      }

      g_myMap[posY][posX]             = view[0][0];
      g_myMap[posY][(posX+1)%S]       = view[0][1];
      g_myMap[(posY+1)%S][posX]       = view[1][0];
      g_myMap[(posY+1)%S][(posX+1)%S] = view[1][1];
    }

    /**
     * guess my start crossroads
     *
     * @return [bool] it's start crossrods or not.
     */
    int guess(int y, int x) {
      vector<int> coord(2);
      coord[0] = y;
      coord[1] = x;
      return ac.guess(coord);
    }

    void showAround(int size = 2) {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"      Look Around      \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = -size; y <= size; y++) {
        for (int x = -size; x <= size; x++) {
          fprintf(stderr,"%c", g_myMap[(posY+y+S)%S][(posX+x+S)%S]);
        }
        fprintf(stderr,"\n");
      }
    }

    void showOldCityMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"     Old City Map      \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", g_oldCityMap[y].c_str());
      }
    }

    void showCityMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"     New City Map      \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", g_cityMap[y].c_str());
      }
    }

    void showMyMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"         My Map        \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", g_myMap[y].c_str());
      }
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
