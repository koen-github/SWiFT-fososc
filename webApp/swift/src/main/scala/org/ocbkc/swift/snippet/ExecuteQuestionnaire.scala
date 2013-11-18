package org.ocbkc.swift.snippet
{
import _root_.scala.xml._
import _root_.net.liftweb.util._
//import _root_.net.liftweb.util.BindHelpers._
import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import org.ocbkc.questionnaire._
import org.ocbkc.swift.global.Logging._
import net.liftweb.mapper._
import Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js._

class ExecuteQuestionnaire
{  
	
		var freetextanswer:Map[Int,String] = Map[Int, String]()
  var mutlipleOptionsanswer = ""
	def renderQuestions(ns: NodeSeq, questionnaire:Questionnaire):NodeSeq = 
   {  val questions:List[Question] = Questionnaire_Question_join.findAll( By(Questionnaire_Question_join.questionnaire, questionnaire)).map( join => join.question.obj.open_! )
		
		
      val ret = questions.flatMap
      {  question =>
         {  // generate html
            val questionTemplate = chooseTemplate("top", "question", ns)
            log("   questionTemplate = " + questionTemplate)
            val bindResultQuestionTemplate = bind( "question", questionTemplate,
               "orderNumber"  -> Text(question.id.is +")"),
               "body"         ->
               {  // retrieve additional info based on the type of question
                                   
                  question.questionType.is match
                  {  case 2 =>
                     {  
						 val questionTemplates = chooseTemplate("top", "questionTemplates", TemplateFinder.findAnyTemplate(List("questionnaire", "question")).open_!)
                        
                        val ftcqTemplate = chooseTemplate("question", "multipleChoiceQuestion", questionTemplates)

val multiAnswers = MultipleChoiceAnswer.findAll( By(MultipleChoiceAnswer.question_id, question.multipleChoiceQuestion.is)).map(_.answerFormulation.is)
		
//val database_answers = multiAnswers(question.id.toInt).answers.answer.i
log("Answer with question id: " +question.id.is +"   "+multiAnswers)
                        val bindResultFtcqTemplate = bind( "mcq", ftcqTemplate,
                           "text"      -> Text(question.questionFormulation.is),
                           "option"  -> SHtml.ajaxSelect(multiAnswers.flatMap(x => Seq(x -> x)).toSeq, Empty, optionSelected => multiUpdate(optionSelected)) //I just wrote my own Seq converter :) :P
                        )
                        log("   ftcqTemplate after bind = " + bindResultFtcqTemplate)
                        bindResultFtcqTemplate
						 
						 
                     }

                     case 1 =>
                     {  val questionTemplates = chooseTemplate("top", "questionTemplates", TemplateFinder.findAnyTemplate(List("questionnaire", "question")).open_!)
                        log("   questionTemplates = " + questionTemplates)
                        val ftcqTemplate = chooseTemplate("question", "freeTextClosedQuestion", questionTemplates)

                        log("   ftcqTemplate = " + ftcqTemplate )
                        val bindResultFtcqTemplate = bind( "ftcq", ftcqTemplate,
                           "text"      -> Text(question.questionFormulation.is),
                           "answerTf"  -> SHtml.ajaxText("", v=>textupdate(Map(question.id.is.toInt -> v.toString)), "id" -> question.id.is.toString)
                        )
                        log("   ftcqTemplate after bind = " + bindResultFtcqTemplate)
                        bindResultFtcqTemplate
                     }
                  }

               }
            )
            log("   questionTemplate after bind: " + bindResultQuestionTemplate)
            bindResultQuestionTemplate
         }
      }
      log("   renderQuestions return value = " + ret)
      ret
   }

   def render(ns: NodeSeq) =
   {//{ TEST: simply pick the first questionnaire
      val qns = Questionnaire.findAll
      val firstQn = qns match
       {
        case qn::restQ => qn
       case Nil       => logAndThrow("No test questionnaire found")
      
      }
    //}

   bind("top", ns,
         "qnName"    ->      Text(firstQn.name.is),
         "question"  -> Questionnaire.findAll.flatMap(x => renderQuestions(ns, x)) ,
         "submitBt"  -> SHtml.submit("Send Answers",procQuestion) //todo: OVERWRITE ALL TEMP ANSWERS AFTER CLICKING THIS BUTTON?
      )
   }
   
   def procQuestion()
   {
	   S.notice(freetextanswer.toString());
	   //todo what?, Yes; todo what?
	   
   }
  def textupdate(s:Map[Int,String]){
    println("Free text anxwer" + s);    //todo write to database
     //MAP: QUESTION ID, USERANSWER     //TODO: WORK WITH SESSIONS PER PAGE LOAD?

  }
  def multiUpdate(d:String) {
    println("Mutli Option selected: "+d)

  }
   def procSelection(selectOption:String){
	   S.notice(selectOption +" ==>  MulitpleOptionAnswer");
   }

  def ajaxLiveText(value: String, func: String => JsCmd, attrs: (String,      String)*): Elem = {
    S.fmapFunc(S.SFuncHolder(func)) {
      funcName =>
      (attrs.foldLeft(<input type="text" value={value}/>)(_ %
        _)) %
        ("onkeyup" -> makeAjaxCall(JE.JsRaw("'" + funcName +
          "=' + encodeURIComponent(this.value)") ))
    }
  }
}

}
