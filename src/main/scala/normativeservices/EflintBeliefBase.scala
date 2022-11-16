package io.github.mostafamohajeri
package normativeservices

import bb.IBeliefBase
import bb.expstyla.exp.{GenericTerm, ListTerm, StructTerm, VarTerm}
import infrastructure.{AkkaMessageSource, ExecutionContext, QueryResponse}
import prolog.terms.Trail
import util.{DutyViolation, EFLINT, InvariantViolation, ServerState, TriggerViolation}

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.postfixOps


class EflintBeliefBase(file:String, debugMode: Boolean = true) extends IBeliefBase[GenericTerm] {
  val eflint_server = new EFLINT(file, debugMode)

  def query_phrase(phrase: String) : ServerState = {
    eflint_server.phrase(phrase)
  }

  override def forceAssertOne(term: GenericTerm): Unit =
    this.synchronized {
      val phrase="+"+term.getStringValue
      val state = query_phrase(phrase)
    }

  override def assertOne(term: GenericTerm) (implicit executionContext: ExecutionContext) : Boolean =
    this.synchronized {
      val phrase="+"+term.getStringValue
      val state = query_phrase(phrase)
      createEvents(state)
      false
    }

  def amend(term: GenericTerm)(implicit executionContext: ExecutionContext) : Boolean =
    this.synchronized {
      val phrase = term
      val state = query_phrase(phrase.getStringValue)
      createEvents(state)
      false
    }

  override def assert(terms: List[GenericTerm]) (implicit executionContext: ExecutionContext) : Unit =
    this.synchronized {
      terms.foreach(t => forceAssertOne(t))
    }

  override def retractOne(term: GenericTerm)(implicit executionContext: ExecutionContext) : Boolean =
    this.synchronized
    {
      val phrase="-"+term.getStringValue
      val state = query_phrase(phrase)
      createEvents(state)
      false
    }

  override def query(term: GenericTerm) (implicit executionContext: ExecutionContext) : QueryResponse =
    this.synchronized
    {

        val queries = ASCToEFlintStringifier.createString(term)
          .split("&&").toList

        val checks = queries.filter(s=>s.contains("?"))
        val act = queries.filter(s => !s.contains("?"))

        val checkResults = checks.map(q => query_phrase(q).lastQuerySucceeded())

        if(checkResults.contains(false))
          return QueryResponse(result = false, Map(), Option.empty)

        else if(act.isEmpty)
          return QueryResponse(result = true, Map(), Option.empty)

        else {
          val res = query_phrase(act.head)
          createEvents(res)
          return QueryResponse(result = true, Map(), Option.empty)
        }

    }

  override def bufferedQuery(term: GenericTerm) (implicit executionContext: ExecutionContext) : Iterator[QueryResponse] =
    this.synchronized
    {
      term match {
        case StructTerm("member",l) =>
          l.last.ref match {
            case ListTerm(items) => items.map(t => QueryResponse(result = true,Map(l.head.asInstanceOf[VarTerm].name -> t))).iterator
          }
      }
    }


  def createEvents(state: ServerState)(implicit executionContext: ExecutionContext): Unit = {

    state.get_output_events()
      .asScala
      .toSeq
      .map(t => StructTerm("triggered",Seq(ASCToEFlintStringifier.parseValue(t.info.value))))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("!"+term)
          val subGoal = executionContext.goalParser.create_goal_message(
            term,
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_made_assignments()
      .asScala
      .toSeq
      .filter(a => a.assignment)
      .map(t => ASCToEFlintStringifier.parseValue(t.value))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("+"+term)
          val subGoal = executionContext.goalParser.create_belief_message(
            term.asInstanceOf[StructTerm],
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_made_assignments()
      .asScala
      .toSeq
      .filter(a => !a.assignment)
      .map(t => ASCToEFlintStringifier.parseValue(t.value))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("-"+term)
          val subGoal = executionContext.goalParser.create_unbelief_message(
            term.asInstanceOf[StructTerm],
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_new_duties()
      .asScala
      .toSeq
      .map(t => StructTerm("handle_new_duty",Seq(
        ASCToEFlintStringifier.parseValue(t.arguments.get(0)),
        ASCToEFlintStringifier.parseValue(t.arguments.get(1)),
        ASCToEFlintStringifier.parseValue(t))
      ))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("!"+term)
          val subGoal = executionContext.goalParser.create_goal_message(
            term.asInstanceOf[StructTerm],
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_violations()
      .asScala
      .toSeq
      .map{
        case d: DutyViolation => StructTerm("handle_duty_violation",Seq(ASCToEFlintStringifier.parseValue(d.duty)))
        case d: InvariantViolation => StructTerm("handle_invariant_violation",Seq(ASCToEFlintStringifier.parseAsString(d.invariant)))
        case d: TriggerViolation => StructTerm("handle_trigger_violation",Seq(ASCToEFlintStringifier.parseValue(d.getInfo.value)))
        case _ => StructTerm("unknown_event",Seq())
      }
      .foreach(
        term => {
//          println("!  "+term)
          val subGoal = executionContext.goalParser.create_goal_message(
            term,
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_new_enabled()
      .asScala
      .toSeq
      .map(t => StructTerm("handle_new_enabled",Seq(ASCToEFlintStringifier.parseValue(t))))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("!"+term)
          val subGoal = executionContext.goalParser.create_goal_message(
            term,
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )

    state.get_new_disabled()
      .asScala
      .toSeq
      .map(t => StructTerm("handle_new_disabled",Seq(ASCToEFlintStringifier.parseValue(t))))
      .filter(t => t.isInstanceOf[StructTerm])
      .foreach(
        term => {
//          println("!"+term)
          val subGoal = executionContext.goalParser.create_goal_message(
            term,
            AkkaMessageSource(executionContext.agent.self)
          )
          if(subGoal.isDefined)
            executionContext.agent.self ! subGoal.get
        }
      )


  }


  override def query(): QueryResponse = QueryResponse(result = true, Map[String, GenericTerm]())

  override def matchTerms(term1: GenericTerm, term2: GenericTerm): QueryResponse = {
    val t1 = term1.getTermValue
    val t2 = term2.getTermValue

    val tr = new Trail()
    if (t1.unify(t2, tr)) {
      QueryResponse(
        result = true,
        tr.substitutions() map { v => v._1 -> GenericTerm.create(v._2.ref) }
      )
    } else {
      QueryResponse(result = false, Map[String, GenericTerm]())
    }
  }

  override def matchTerms(): QueryResponse = QueryResponse(result = true, Map[String, GenericTerm]())

}




