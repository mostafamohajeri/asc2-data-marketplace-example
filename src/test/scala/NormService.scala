package io.github.mostafamohajeri

import normativeservices.{EFlintBBFactory, Environment, PlantUMLCommunicationLogger}

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.util.Timeout
import asl.{participant, participant_advisor}
import bb.expstyla.exp.{IntTerm, StringTerm, StructTerm}
import infrastructure._
import org.scalatest.wordspec.AnyWordSpecLike
import std.DefaultCommunications

import _root_.scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class NormService extends ScalaTestWithActorTestKit with AnyWordSpecLike {

//  import org.apache.log4j.BasicConfigurator
//
//  BasicConfigurator.configure()

  val logger = PlantUMLCommunicationLogger()
  val loggableComs = new DefaultCommunications(logger = logger)
  Environment.comsLogger = logger
  Environment.logActors(Seq("A","AAdvisor","B","BAdvisor","C","CAdvisor","D","DAdvisor"))

  val mas = MAS()

  override def beforeAll(): Unit = {
    // Create System

    val system: ActorSystem[IMessage] = typed.ActorSystem(mas(), "MAS")

    val BB_A = new EFlintBBFactory("src/main/eflint/data-sharing-contract-alice-role.eflint")
    val BB_B = new EFlintBBFactory("src/main/eflint/data-sharing-contract-bob-role.eflint")
    val BB_C = new EFlintBBFactory("src/main/eflint/data-sharing-contract-charlie-role.eflint")
    val BB_D = new EFlintBBFactory("src/main/eflint/data-sharing-contract-david-role.eflint")
    //    ClusterBootstrap(system).start()
    implicit val timeout: Timeout = 5000.milliseconds
    implicit val ec: ExecutionContextExecutor = system.executionContext
    implicit val scheduler: Scheduler = system.scheduler

    // Ask the system to create agents
    val result: Future[IMessage] = system.ask(ref => AgentRequestMessage(
      Seq(
        AgentRequest(new participant_advisor(beliefBaseFactory = BB_A,coms = loggableComs).agentBuilder, "AAdvisor", 1),
        AgentRequest(new participant(coms = loggableComs).agentBuilder, "A", 1),
        AgentRequest(new participant_advisor(beliefBaseFactory = BB_B,coms = loggableComs).agentBuilder, "BAdvisor", 1),
        AgentRequest(new participant(coms = loggableComs).agentBuilder, "B", 1),
        AgentRequest(new participant_advisor(beliefBaseFactory = BB_C,coms = loggableComs).agentBuilder, "CAdvisor", 1),
        AgentRequest(new participant(coms = loggableComs).agentBuilder, "C", 1),
        AgentRequest(new participant_advisor(beliefBaseFactory = BB_D,coms = loggableComs).agentBuilder, "DAdvisor", 1),
        AgentRequest(new participant(coms = loggableComs).agentBuilder, "D", 1),

      ), ref))(timeout, scheduler)
    //wait for response
    println("WAITING RESPONSE")
    val system_ready: Boolean = try {
      val response = Await.result(result, timeout.duration).asInstanceOf[ReadyMessage]
      println("RESPONSE:" + response)
      true
    }
    catch {
      case x: Throwable =>
        x.printStackTrace()
        false
    }

    if (system_ready)
      println("agent created")

  }

  "the agents" should {
    "exist in yellow pages if it was created before" in {
      assert(mas.yellowPages.getAgent("CAdvisor").isDefined)
      assert(mas.yellowPages.getAgent("C").isDefined)
    }
  }
//
  "the participants" should {
    "init contract" in {
      val prob = testKit.createTestProbe[IMessage]()
      mas.yellowPages.getAll().filter(a => !a._1.contains("Advisor")).foreach(agent => {
        agent._2.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
          StructTerm("init", Seq()), AkkaMessageSource(prob.ref)
        )
      })
    }
  }



//  "notary service advisor" should {
//    "init" in {
//      val prob = testKit.createTestProbe[IMessage]()
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("citizen", Seq(StringTerm("Alice"))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("citizen", Seq(StringTerm("Bob"))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(1000)
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("property", Seq(StringTerm("A1"), IntTerm(3))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("mortgage", Seq(StringTerm("Alice"), StructTerm("property", Seq(StringTerm("A1"), IntTerm(3))))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("occupant", Seq(StringTerm("Alice"), StructTerm("property", Seq(StringTerm("A1"), IntTerm(3))))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("property", Seq(StringTerm("B1"), IntTerm(2))))), AkkaMessageSource(prob.ref)
//      )
////
////      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
////        StructTerm("data", Seq(StructTerm("occupant", Seq(StringTerm("Bob"), StructTerm("property", Seq(StringTerm("B1"), IntTerm(2))))))), AkkaMessageSource(prob.ref)
////      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("mortgage", Seq(StringTerm("Bob"), StructTerm("property", Seq(StringTerm("B1"), IntTerm(2))))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(1000)
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("first_property", Seq(StringTerm("Alice"), StructTerm("property", Seq(StringTerm("A1"), IntTerm(3))))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("first_property", Seq(StringTerm("Bob"), StructTerm("property", Seq(StringTerm("B1"), IntTerm(2))))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(3000)
//    }
//
//    "add property" in {
//      val prob = testKit.createTestProbe[IMessage]()
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("property", Seq(StringTerm("C1"), IntTerm(7))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(3000)
//    }
//
//
//    "Bob moving" in {
//      val prob = testKit.createTestProbe[IMessage]()
//      mas.yellowPages.getAgent("Amsterdam").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("register_occupant", Seq(StringTerm("Bob"),StructTerm("property", Seq(StringTerm("A1"), IntTerm(3))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(3000)
//    }
//
//    "new rule 3.a"  in {
//      val prob = testKit.createTestProbe[IMessage]()
//
//      mas.yellowPages.getAgent("NotaryService").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("amend_service", Seq(StringTerm("Extend Fact article_one Conditioned by article_three()"))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(1000)
//
//    }
//
//    "Chloe tries" in {
//      val prob = testKit.createTestProbe[IMessage]()
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("citizen", Seq(StringTerm("Chloe"))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("property", Seq(StringTerm("C1"), IntTerm(2))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("mortgage", Seq(StringTerm("Chloe"), StructTerm("property", Seq(StringTerm("C1"), IntTerm(2))))))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("Notary").get.asInstanceOf[AkkaMessageSource].address() ! BeliefMessage(
//        StructTerm("data", Seq(StructTerm("first_property", Seq(StringTerm("Chloe"), StructTerm("property", Seq(StringTerm("C1"), IntTerm(2))))))), AkkaMessageSource(prob.ref)
//      )
//
//      Thread.sleep(1000)
//    }
//
//    "new rules 3.b" in {
//      val prob = testKit.createTestProbe[IMessage]()
//
//      mas.yellowPages.getAgent("NotaryService").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("amend_service", Seq(StringTerm("Fact undue_cancel_nim Identified by citizen * mortgage"))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("NotaryService").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("amend_service", Seq(StringTerm("Duty duty_to_cancel_nim Holder citizen Claimant notary  Related to mortgage, occupant When citizen == mortgage.citizen && citizen == occupant.citizen &&  occupant.citizen == mortgage.citizen Violated when undue_cancel_nim() Holds when (nim_covered(mortgage) && mortgage.property.address != occupant.property.address)"))), AkkaMessageSource(prob.ref)
//      )
//
//      mas.yellowPages.getAgent("NotaryService").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("amend_service", Seq(StringTerm("Event cancel_nim_delay  Related to citizen, mortgage  Creates undue_cancel_nim() When duty_to_cancel_nim()"))), AkkaMessageSource(prob.ref)
//      )
//
//      println("AMENDING COMPLETE")
//
//      Thread.sleep(3000)
//
//    }
//
//
//    "Bob forgets" in {
//      val prob = testKit.createTestProbe[IMessage]()
//      mas.yellowPages.getAgent("NotaryService").get.asInstanceOf[AkkaMessageSource].address() ! GoalMessage(
//        StructTerm("normative_event", Seq(StructTerm("cancel_nim_delay",Seq(StringTerm("Bob"),StructTerm("mortgage", Seq(StringTerm("Bob"), StructTerm("property", Seq(StringTerm("B1"), IntTerm(2))))))))), AkkaMessageSource(prob.ref)
//      )
//      Thread.sleep(2000)
//    }
//
//  }


  override def afterAll(): Unit = {

    Thread.sleep(5000)

    println("WRITING TO FILE ...")
    // write the message to file "./logs/<name>.png"
    Environment.print_to_file("all")
//    testKit.shutdownTestKit()
  }
}