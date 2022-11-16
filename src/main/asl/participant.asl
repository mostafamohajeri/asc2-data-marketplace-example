+!init =>
    Advisor = #asString(Self) + "Advisor";
    +advisor(Advisor);
    !inform_advisor(event,init_contract).

+!send_data(Target,Data) =>
    !inform_advisor(act,send_data(Self,Target,Data));
    #println("Sending " + Data + " to " + Target);
    #coms.inform(Target,package(Data)).

+!compute(Target,Input,Algorithm,Result) =>
    R = Input + Algorithm;
    +ref(Result,R);
    +have(Data);
    !inform_advisor(act,compute(Self,Target,Input,Algorithm,Result)).

+package(Data) =>
    +have(Data);
    #println(Self + " : " + package(Data));
    !inform_advisor(Act,send_data(Source,Self,Data))
    .

+compute(S,T,D,Alg,Ref) : Self == S =>
    #println(Self + " : " + compute(S,T,D,Alg,Ref));
    !compute(T,D,Alg,Ref).

+compute(S,T,D,Alg,Ref) =>
    #println(Self + " : " + compute(S,T,D,Alg,Ref)).

+send_data(S,T,D) : Self == S =>
    #println(Self + " : " + send_data(S,T,D));
    !send_data(T,D)

+send_data(S,T,D) =>
    #println(Self + " : " + send_data(S,T,D)).

+!inform_advisor(event,Event) : advisor(Advisor) =>
    #println("telling " + Advisor + " " + Event);
    #coms.achieve(Advisor,occurred(Event)).

+!inform_advisor(act,Act) : advisor(Advisor) =>
    #println("telling " + Advisor + " " + Act);
    #coms.achieve(Advisor,perform(Act)).




