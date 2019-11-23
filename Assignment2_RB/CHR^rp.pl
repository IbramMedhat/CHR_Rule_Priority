:- use_module(library(chr)).
:- chr_constraint start/0, conflictdone/0,fire/0, id/1, history/1,
a/1,b/1,c/1.

id(I), a <=> a(I), I1 is I+1,id(I1).
id(I), b <=> b(I), I1 is I+1,id(I1).
id(I), c <=> c(I), I1 is I+1,id(I1).

start, a(ID) , b(ID1) ==> match(r0, 0.7,[ID, ID1]).
start, a(ID) , b(ID1), c(ID2) ==> match(r1, 0.3,[ID, ID1, ID2]).

start <=> conflictdone.


history(L),conflictdone\match(R,_,IDs) <=>
member((R,FIDs),L),sort(IDs,II),sort(FIDs,II)|true.

conflictdone,match(_,P1,_)\ match(_,P2,_)
<=> P1<P2 | true.

conflictdone ,match(_,P1,_) \ match(_,P2,_)
<=> P1 = P2, A is random(2), A = 1 | true.


conflictdone <=> fire.

