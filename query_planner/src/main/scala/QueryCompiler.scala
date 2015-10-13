package DunceCap

object QueryCompiler {
  def readFile(file:String): String = {
    val source = scala.io.Source.fromFile(file)
    val line = try source.mkString finally source.close()
    line
  }
  def getInput() : Unit = {
    print(">")
    for (ln <- io.Source.stdin.getLines){
      if(ln == "exit()"){
        return
      } 
      DCParser.run(ln)
      print(">")
    } 
  }
  def main(args:Array[String]) = {
    if( (args.length == 0) || (args.length == 1)){
      if(args.length == 1){
        DCParser.run(readFile(args(0)))
      } else {
        getInput()
      }
    } else{
      println("""Usage: "./QueryCompiler" or "./QueryCompiler <datalog file>" """)
    }
  }
}
