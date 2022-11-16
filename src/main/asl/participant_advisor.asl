+?permitted(A): enabled(A) => #coms.respond(true).

+?permitted(A) => #coms.respond(false).

+!perform(A): enabled(A) =>
    #println("performing: " + A);
    #io.github.mostafamohajeri.NormUtils.perform(A).

+!occurred(A) =>
    #println("recording: " + A);
    #io.github.mostafamohajeri.NormUtils.perform(A).

+!perform(A) =>
    #println("NOT performing: " + A);
    #coms.inform(#executionContext.src, failed(A)).

+?holds(A): holds(A) => #coms.respond(true).

+?holds(A) => #coms.respond(false).

+to_send_data(party(S),party(T),data(D)) =>
    ParentName = #asString(Self).replace("Advisor","");
    #coms.inform(ParentName,send_data(S,T,D)).


+to_compute(party(S),party(T),data(D),data(Alg),reference(Ref)) =>
    ParentName = #asString(Self).replace("Advisor","");
    #coms.inform(ParentName,compute(S,T,D,Alg,Ref)).

-to_send_data(party(S),party(T),data(D)) =>
    ParentName = #asString(Self).replace("Advisor","");
    #coms.un_inform(ParentName,send_data(S,T,D)).


-to_compute(party(S),party(T),data(D),data(Alg),reference(Ref)) =>
    ParentName = #asString(Self).replace("Advisor","");
    #coms.un_inform(ParentName,compute(S,T,D,Alg,Ref)).