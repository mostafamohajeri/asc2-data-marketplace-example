## Data market-place example with ASC2 and eFLINT
This is an example of to model a complex data-sharing scenario with these tools.

## Scenario (1)
* Four participants: **A,B,C,D**
* David (**D**) is in need of some synthesized (or a trained model) called **ResultFinal**.
* Alice (**A**) has two algorithms (**Alg1**,**Alg2**) that when used in a sequence can produce the **ResultFinal** for David
* But Alice needs the input data **Data1** as input to **Alg1** to create the **Result1** and then feed that to **Alg2** to create the model
* Bob (**B**) has **Data1**
* Bob does not trust Alice with the data and Alice does not trust bob with the algorithms
* Both trust Charlie (**C**) that also has the infrastructure to run everything, so in the scenario:
  * A sends Alg1 to C
  * B sends Data1 to C
  * C runs Alg1 on Data1 and creates Result1
  * C sends Result1 to A
  * A runs Alg2 on Result1 and creates ResultFinal
  * A sends ResultFinal to D

## Implementation (1)

Each agent gets a very specific and detailed advisor about their role (e.g.,`data-sharing-contract-charlie-role.eflint`), the advisor only provides local information to agent about 
its role and nothing more. This means 100% privacy about internal state and no leakage of meta-data about policies.
Also, the advisor can be specialized for each agent based on the local regulations if needed. We can see a local knowledge 
state of each agent with the local transmission logger of ASC2, for example from the start the 
only thing **D** knows is that at some point it will receive a data called **ResultFinal** and is never aware of anything that is happening in between, or, when Alice sends Alg1 to Charlie, she only knows that at some point it will receive Result1, regardless of the rest of the agents.

Executing the MAS generates this:
![Sequence Diagram](logs/all.png?raw=true "Title")

### Question: can we automate this process from something simpler? even graphical?

## Alternative (2)
Instead of a local one, we can make a central instance that filters information before sending to agents (is it easier to do so?)

## Alternative (3) 
Instead of local one, can we make advisors about bilateral connections? like the sale example so agents share advisors and also some meta-data about policy