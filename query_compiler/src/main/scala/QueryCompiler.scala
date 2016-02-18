package duncecap

import java.io._

import scala.collection.mutable.ListBuffer


case class Config(directory: Option[String] = None,
                  dbConfig:String = "",
                  nprrOnly:Boolean = false,
                  bagDedup:Boolean = true,
                  codeGen:Option[String] = None,
                  readQueryFromFile:Boolean = false,
                  query:String = "",
                  explain:Boolean = false)

//Defines schema types for relations
case class Schema(
  val externalAttributeTypes:List[String], 
  val externalAnnotationTypes:List[String]
  ) extends Serializable {
  val attributeTypes:List[String] = checkAttributes()
  val annotationTypes:List[String] = checkAnnotations()
  def getAttributeTypes:Array[String] = {externalAttributeTypes.toArray}
  def getAnnotationTypes:Array[String] = {externalAnnotationTypes.toArray}
  
  //Checks that the schema has valid types.
  private def checkAttributes():List[String] = {
    externalAttributeTypes.map(a => {
      if(!QueryCompiler.validAttributeTypes.contains(a))
        throw new Exception("Attribute type " + a + " in schema is not valid.")
      QueryCompiler.validAnnotationTypes(a)
    })
  }
  private def checkAnnotations():List[String] = {
    externalAnnotationTypes.map(a => {
      if(!QueryCompiler.validAnnotationTypes.contains(a))
        throw new Exception("Attribute type " + a + " in schema is not valid.")
      QueryCompiler.validAnnotationTypes(a)
    })
  }
}

//Defines relations
case class Relation(
  val name:String,  
  val schema:Schema,
  val filename:String,
  val df:Boolean
  ) extends Serializable {
  def getName():String = {name}
  def getSchema():Schema = {schema}
  def getFilename():String = {filename}
  def getDF():Boolean = {df}
}

//Configuration for the db
case class Config(
  val system:String, 
  val numThreads:Int, 
  val numSockets:Int,
  val layout:String,
  val memory:String
  ) extends Serializable {
  def getSystem():String = {system}
  def getNumThreads():Int = {numThreads}
  def getNumSockets():Int = {numSockets}
  def getLayout():String = {layout}
  def getMemory():String = {memory}
}

//Creates an instance of database (needed to compile queries)
case class DBInstance(val folder:String, val config:Config) extends Serializable {
  val relations:ListBuffer[Relation] = ListBuffer[Relation]()
  def addRelation(r:Relation){
    relations += r
  }
  def getFolder():String = {folder}
  def getConfig():Config = {config}
  def getNumRelations():Int = {relations.length}
  def getRelation(i:Int) = {relations(i)}
}

//Main class which compilation runs out of
class QueryCompiler(val db:DBInstance, val hash:String) extends Serializable{
  def getDBInstance():DBInstance = {db}

  def createDB(){
    CreateDB.loadAndEncode(db,hash)
  }

  //Parse a datalog statement and code generate it.
  def datalog(query:String):String = {
    val ir = DatalogParser.run(query)
    println(ir)
    "Query.cpp"
  }
}

object QueryCompiler {
  //acceptable attribute types
  val validAttributeTypes = Map(
    //"Boolean" -> ,
    //"Byte",
    "uint32" -> "uint32_t",
    "int32" -> "int32_t",
    "int64" -> "uint64_t",
    "uint64" -> "int64_t"
    //"String",
    //"Float",
    //"Double",
    //"Date"
    )

  //All numeric types
  val validAnnotationTypes = Map(
    "uint32" -> "uint32_t",
    "int32" -> "int32_t",
    "int64" -> "int64_t",
    "uint64" -> "uint64_t",
    "float32" -> "float",
    "float64" -> "double"
  )

  //Special builder to conver arrays to lists 
  def buildSchema(attrTypes:Array[String],annoTypes:Array[String]) : Schema = {
    Schema(attrTypes.toList, annoTypes.toList)
  }
  //Build from a schema saved on disk
  def fromDisk(filename:String) : QueryCompiler = {
    val fos = new FileInputStream(filename)
    val oos = new ObjectInputStream(fos)
    val myqc = oos.readObject().asInstanceOf[QueryCompiler]
    oos.close()
    myqc
  }
}
