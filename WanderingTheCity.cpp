#include <iostream>
#include <vector>
#include <algorithm>
#include <cassert>
#include <string.h>

using namespace std;

int S;

const int HIT = 1;
const int FILTER_SIZE = 4;
const double SAME_RATE = 0.97;

class Actions {
  public:
    vector<string> look() {
      cout << "?look" << endl;
      vector<string> ret(2);
      cin >> ret[0]; cin >> ret[1];
      return ret;}
    int walk(vector<int> shift) {
      cout << "?walk" << endl;
      cout << shift[0] << endl;
      cout << shift[1] << endl;
      int result; cin >> result; return result;}
    int guess(vector<int> coord) {
      cout << "?guess" <<endl;
      cout << coord[0] << endl;
      cout << coord[1] << endl;
      int result; cin >> result; return result;}
};

struct Coord {
  int y;
  int x;

  Coord(int y = -1, int x = -1) {
    this->y = y;
    this->x = x;
  }
};

class WanderingTheCity {
  public:
    Actions ac;
    int posY;
    int posX;
    int currentCost;
    int walkCost;
    int lookCost;
    int guessCost;
    vector<string> oldCityMap;
    vector<string> cityMap;
    vector<string> myMap;

    void init(vector<string> cityMap, int W, int L, int G) {
      S = cityMap.size();
      oldCityMap = cityMap;
      cityMap = cityMap;
      myMap = cityMap;
      walkCost = W;
      lookCost = L;
      guessCost = G;
      currentCost = 0;

      for (int y = 0; y < S; y++) {
        for (int x = 0; x < S; x++) {
          myMap[y][x] = '-';
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

      //showOldCityMap();
      showMyMap();
      //showAround();

      vector<string> filter = createFilter(0, 0);
      showFilter(filter);

      vector<Coord> result = scan(filter);
      int rsize = result.size();
      fprintf(stderr,"rsize = %d\n", rsize);
      int repeatI = estimateRepeatI();
      int repeatJ = estimateRepeatJ();
      fprintf(stderr,"repeatI = %d, repeatJ = %d\n", repeatI, repeatJ);
      cerr.flush();

      for (int i = 0; i < rsize; i++) {
        Coord coord = result[i];
        int result = guess(coord.y, coord.x);
        //fprintf(stderr,"(%d, %d)\n", coord.y, coord.x);
        if (result == HIT) {
          return 0;
        }
      }

      return 0;
    }

    /**
     * estimate city repeatI (y axis)
     *
     * @return [int] repeatI
     */
    int estimateRepeatI() {
      for (int i = 2; i < S; i++) {
        if (S % i != 0) continue;

        int totalCnt = 0;
        int sameCnt = 0;

        for (int x = 0; x < S; x++) {
          char ch = oldCityMap[0][x];

          for (int y = 0; y < S; y+=i) {
            totalCnt++;

            if (ch == oldCityMap[y][x]) {
              sameCnt++;
            }
          }
        }

        double rate = sameCnt / (double) totalCnt;
        if (rate >= SAME_RATE) {
          return i;
        }
      }

      return S;
    }

    /**
     * estimate city repeatJ (x axis)
     *
     * @return [int] repeatJ
     */
    int estimateRepeatJ() {
      for (int j = 2; j < S; j++) {
        if (S % j != 0) continue;

        int totalCnt = 0;
        int sameCnt = 0;

        for (int y = 0; y < S; y++) {
          char ch = oldCityMap[y][0];

          for (int x = 0; x < S; x+=j) {
            totalCnt++;

            if (ch == oldCityMap[y][x]) {
              sameCnt++;
            }
          }
        }

        double rate = sameCnt / (double) totalCnt;
        if (rate >= SAME_RATE) {
          return j;
        }
      }

      return S;
    }

    vector<string> createFilter(int y, int x) {
      vector<string> filter(4, "----");

      for (int i = 0; i < FILTER_SIZE; i++) {
        for (int j = 0; j < FILTER_SIZE; j++) {
          filter[i][j] = myMap[(y+i)%S][(x+j)%S];
        }
      }

      return filter;
    }

    vector<Coord> scan(vector<string> &filter) {
      vector<Coord> coords;

      for (int y = 0; y < S; y++) {
        for (int x = 0; x < S; x++) {
          int value = 0;

          for (int i = 0; i < FILTER_SIZE; i++) {
            for (int j = 0; j < FILTER_SIZE; j++) {
              if (filter[i][j] == oldCityMap[(y+i)%S][(x+j)%S]) {
                value++;
              }
            }
          }

          if (value >= 12) {
            coords.push_back(Coord(y, x));
          }
        }
      }

      return coords;
    }

    /**
     * walking this map.
     */
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
      shifts[0] = shiftY;
      shifts[1] = shiftX;

      //fprintf(stderr,"move... (%d, %d) => (%d, %d)\n", posY, posX, ny, nx);
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

      /*
      for (int y = 0; y < 2; y++) {
        fprintf(stderr,"%s\n", view[y].c_str());
      }
      */

      myMap[(posY-1+S)%S][(posX-1+S)%S] = view[0][0];
      myMap[(posY-1+S)%S][posX]         = view[0][1];
      myMap[posY][(posX-1+S)%S]         = view[1][0];
      myMap[posY][posX]                 = view[1][1];
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
          fprintf(stderr,"%c", myMap[(posY+y+S)%S][(posX+x+S)%S]);
        }
        fprintf(stderr,"\n");
      }
    }

    void showOldCityMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"     Old City Map      \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", oldCityMap[y].c_str());
      }
    }

    void showCityMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"     New City Map      \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", cityMap[y].c_str());
      }
    }

    void showMyMap() {
      fprintf(stderr,"-----------------------\n");
      fprintf(stderr,"         My Map        \n");
      fprintf(stderr,"-----------------------\n");
      for (int y = 0; y < S; y++) {
        fprintf(stderr,"%s\n", myMap[y].c_str());
      }
    }

    void showFilter(vector<string> &filter) {
      for (int i = 0; i < FILTER_SIZE; i++) {
        fprintf(stderr,"%s\n", filter[i].c_str());
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
  wtc.whereAmI(cityMap, W, L, G);
  cout << "!" << endl;
}
