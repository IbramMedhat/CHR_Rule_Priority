:- use_module(library(chr)).
:- chr_constraint start/0, conflictdone/0,fire/0, id/1, history/1,
a/1,b/1,c/1,d/1.

id(I), a <=> a(I), I1 is I+1,id(I1).
id(I), b <=> b(I), I1 is I+1,id(I1).
id(I), c <=> c(I), I1 is I+1,id(I1).
id(I), d <=> d(I), I1 is I+1,id(I1).

r1@ start, a(ID) , b(ID1), c(ID2) ==> match(r1, 0.7,[ID, ID1, ID2]).
r2@ start, a(ID) , b(ID1), c(ID2), d(ID3) ==> match(r2, 0.3,[ID, ID1, ID2, ID3]).

start <=> conflictdone.


history(L),conflictdone\match(R,_,IDs) <=>
member((R,FIDs),L),sort(IDs,II),sort(FIDs,II)|true.

conflictdone,match(_,P1,_)\ match(_,P2,_)
<=> P1<P2 | true.

conflictdone ,match(_,P1,_) \ match(_,P2,_)
<=> P1 = P2, A is random(2), A = 1 | true.


conflictdone <=> fire.

a(ID),b(ID1)\c(ID2),history(L),fire,match(r1,_,[ID, ID1, ID2])
a(ID),b(ID1),c(ID2),d(ID3),history(L),fire,match(r2,_,[ID, ID1, ID2, ID3])
