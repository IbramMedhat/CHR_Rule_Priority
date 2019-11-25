:- use_module(library(chr)).
:- chr_constraint start/0, conflictdone/0,fire/0, id/1, history/1, match/3,
a/1,a/0,b/1,b/0,c/1,c/0,e/0,h/0,d/1,d/0,f/1,f/0,g/0.

id(I), a <=> a(I), I1 is I+1,id(I1).
id(I), b <=> b(I), I1 is I+1,id(I1).
id(I), c <=> c(I), I1 is I+1,id(I1).
id(I), d <=> d(I), I1 is I+1,id(I1).
id(I), f <=> f(I), I1 is I+1,id(I1).

r1@ start, a(ID) , b(ID1), c(ID2) ==> match(r1,1,[ID, ID1, ID2]).
r2@ start, a(ID) , b(ID1), c(ID2) ==> match(r2,1,[ID, ID1, ID2]).
r3@ start, d(ID) , f(ID1) ==> match(r3,2,[ID, ID1]).

start <=> conflictdone.


history(L),conflictdone\match(R,_,IDs) <=>
member((R,FIDs),L),sort(IDs,II),sort(FIDs,II)|true.

conflictdone,match(_,P1,_)\ match(_,P2,_)
<=> P1<P2 | true.

conflictdone ,match(_,P1,_) \ match(_,P2,_)
<=> P1 = P2, A is random(2), A = 1 | true.


conflictdone <=> fire.

a(ID),b(ID1)\c(ID2),history(L),fire,match(r1,_,[ID, ID1, ID2]) <=> e, history([(r1,[ID, ID1, ID2])|L]),start.
a(ID),b(ID1),c(ID2)\history(L),fire,match(r2,_,[ID, ID1, ID2]) <=> e, h, history([(r2,[ID, ID1, ID2])|L]),start.
d(ID),f(ID1),history(L),fire,match(r3,_,[ID, ID1]) <=> g, history([(r3,[ID, ID1])|L]),start.
