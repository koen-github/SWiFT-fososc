package org.ocbkc.swift 
{
package snippet 
{
import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import org.ocbkc.swift.lib._
import org.ocbkc.swift.model.Player
import Helpers._
import System.err.println
import org.ocbkc.swift.global.GlobalConstant._
import org.ocbkc.swift.global.TestSettings._
import org.ocbkc.swift.parser._
import scala.util.parsing.combinator.Parsers //{Success, Failure}
// BriCo = BridgeConstruction
object sesBriCo extends SessionVar(new BridgeConstructionSessionInfo)

class BridgeConstructionSessionInfo
{  var subjectTf:String = ""
   var predicateTf:String = ""
}

class BridgeConstruction
{  val sesBriCoLR = sesBriCo.is

   val sesCoordLR = sesCoord.is; // extract session coordinator object from session variable.
   var errorTrans:String = ""
   var translationTAcontents:String = if(!TEST) "Enter translation here." else sesCoordLR.cc.textCTLbyComputer

   def render(ns: NodeSeq): NodeSeq =
   {  if(!sesCoordLR.URstartBridgeConstruction)
         S.redirectTo("TODOpageCorrespondingWithLatestRoundFluencySession")
      
      def processSubmission() = 
      {  println("BridgeConstruction.processSubmission called")
         
         // check errors on submission here
         
         // when errors, do a reload
         val bridgeCorrect:Boolean = sesCoordLR.testSyntaxBridge match
         {  case None         => false
            case Some(pr)     => pr match {  case HurelanBridge.Success(_,_)     => true
                                             case HurelanBridge.Failure(msg,_)   => false
                                          }
         }
         
         println("   bridgeCorrect = " + bridgeCorrect)
         // sesCoord
         // <&y2011.10.23.17:49:39&>
         println("   bridge = " + sesCoordLR.cc.bridgeCTL2NLplayer)
         
         sesCoord.URstopBridgeConstruction
         
         if(bridgeCorrect) S.redirectTo("questionAttackRound.html") else S.redirectTo("bridgeconstruction.html") 
      }

      def processSubjectPlayerTf(contentTf:String) =
      {  // <&y2012.02.10.10:08:37& check here whether the provided subject indeed occurs in the CTL translation of the player. This requires an additional function in the clean part!>
         // ( importance=10
         // )
         println("BridgeConstruction.processSubjectPlayerTf called")
         sesBriCo.subjectTf = contentTf
         sesCoordLR.cc.bridgeCTL2NLplayer = "" // <&y2012.02.21.09:35:55&09:35:55 right location to do this?>

         sesCoordLR.cc.bridgeCTL2NLplayer += "entity(" + contentTf + "," + sesCoordLR.cc.subjectNL + ")\n"
         // <&y2012.02.16.10:00:27& or should I use string builder?>
      }

      def processPredicatePlayerTf(contentTf:String) = 
      {  // <&y2012.02.10.10:09:24& check here whether the provided prediate indeed occurs in the CTL translation of player>
         println("BridgeConstruction.processPredicatePlayerTf called")
         sesBriCoLR.predicateTf = contentTf
         sesCoordLR.cc.bridgeCTL2NLplayer += "hurelan(" + sesCoordLR.cc.hurelanRole1NL + "," + contentTf + "," + sesCoordLR.cc.hurelanRole2NL + ")\n"
      }
      
      def processTestBridgeBt() =
      {  println("BridgeConstruction.processTestBridgeBt called")
         // <&y2012.01.19.09:56:18& is processTranslationTA indeed called before this method, otherwise I have a problem...>/(importance = 10)
          S.redirectTo("translationRound.html")
      }

      // <? why can't scala infer that contentTranslationTA must be a String. (Omitting String, will result in an error).

      def processBridgeCTL2NLtA(contentTranslationTA:String) =
      {  sesCoordLR.cc.bridgeCTL2NLplayer = contentTranslationTA  
         // sesCoordLR.translation = contentTranslationTA // <&y2011.11.17.18:53:43& add check in Coord.scala that the translation has indeed been defined when you start using it somewhere, perhaps with Option>
      }

      /*
      case class SWiFTParseResult
      case class EmptyFile extends SWiFTParseResult
      case class FilledFile(ParseResult) extends SWiFTParseResult
      */
/*
      def testBridgeText:ParseResult = 
      {   if(sesCoordLR.cc.bridgeCTL2NLplayer != "")
               {  FilledFile(testSyntaxBridge) match 
                  { case (correct, msg) => if(correct)  else "Contains errors: " + msg
                  }
               }
               else 
                  EmptyFile
      }
  */    
      def errorBridgeWebText =
      {        Text( sesCoordLR.testSyntaxBridge match
               {  case None         => ""  
                  case Some(pr)     => pr match {  case HurelanBridge.Success(_,_) => "Correct!"
                                                   case HurelanBridge.Failure(msg,_)  => "Contains errors: " + msg
                                                }
               }
             )
         // <&y2012.01.22.13:58:43& change first check to regexp only containing all visibly empty documents (so only containing spaces, enters, tabs etc.)>
      }

      val testExampleBridge = "hurelan(s,p,t)\nentity(c1,Akwasi)"
      //sesCoordLR.cc.bridgeCTL2NLcomputer
      val testExampleTextCTL = "p({a},{b})"

/* <? &y2012.02.22.14:48:06& somehow the following does not work. Why not?>

      val toBeBoundInTheBindFunction = sesCoordLR.cc.constantsByPlayer match 
                                             {  case Some(constants) => SHtml.ajaxSelectElem(constants, Empty)(processPredicatePlayerTf)
                                                case None            => println("   No constants found in translation player."); Text("Your translation doesn't contain any constants")
                                             }


*/



      val constants = sesCoordLR.cc.constantsByPlayer match 
                                             {  case Some(consts) => consts
                                                case None         => println("   No constants found in translation player."); Nil
                                             }

      var boundForm = bind( "form", ns, 
            "translation"                 -> Text(sesCoordLR.cc.textCTLbyPlayer),
            //"subjectCTLbyPlayer"        -> SHtml.text(sesBriCo.subjectTf, processSubjectPlayerTf),
            "subjectCTLbyPlayer"          -> SHtml.select(constants.map(c=>(c,c)), Empty, processSubjectPlayerTf),
            "predicateCTLbyPlayer"        -> ( sesCoordLR.cc.predsByPlayer match 
                                             {  case Some(Nil)       => println("   No constants found in translation player."); Text("Note: your translation doesn't contain any predicates.")

                                                case Some(preds)     => SHtml.select(preds.map(p=>(p,p)), Empty, processPredicatePlayerTf)
                                                case None            => throw new RuntimeException("   Error during parsing of CTL text player, should not happen in this stage.")
                                             }
                                             ),
            //"predicateCTLbyPlayer"        -> Text(sesBriCoLR.predicateTf),
            "constructedBridgeCTL2NL"     -> Text(sesCoordLR.cc.bridgeCTL2NLplayer),
            "errorInInfo2ConstructBridge" -> errorBridgeWebText,
            "submitBt"                    -> SHtml.submit("Submit", processSubmission)
          )
      bind("transround", boundForm, "sourceText" -> Text(sesCoordLR.cc.textNL))
   }
}

}
}
