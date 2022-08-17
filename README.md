# JOLT

### Que es Jolt
Jolt es una herramienta que nos facilita la transformación de JSON a JSON, lo que nos permite transformar JSON provenientes de fuentes como ElasticSearch, MongoDb, Cassandra o colas de mensajes.
Se enfoqué principal es la transformación del JSON, no en la manipulación de los valores que este contenga.

### Comodines *
```bash
* : Este comodin permite iterar las claves de objeto JSON que están al mismo nivel para evitar tener que escribir cada unas de ellas.
& : Este comodín ayuda a la iteración de valores y claves del JSON
$ : Este comodin permite hacer refecncia a la clave, key o atributo del JSON dentros de una iteración
# : Este comomdin permite el ordemaiento del JSON por medio del uso de una matriz
| : Este comodín 'o' permite hacer coincidir varias claves de entrada. Útil si no siempre sabe exactamente cuáles serán sus datos de entrada.
@ : Este comodin hace refeencias a los valotes que pose un atributo del JSON
```

### Transformaciones
Las transformaciones son las que definen el tratamiento que se le dará al JSON. Cada tipo de transformación posee un Spec que define el comportamiento que se deberá realizar al JSON de entrada.


##### Transformaciones Basicas
```bash
shift       : copia los datos y los asigna en una posición nueva 
default     : establece valores por defecto
remove      : retira valores del JSON de entrada 
sort        : ordenar los valores clave del mapa alfabéticamente
cardinality : cambia la cardinalidad de los elementos de datos JSON [listas]
```


### shift

###### entrada
```bash
{
  "rating": {
       "quality": {
          "value": 3,
          "max": 5
       }
    }
}
```

###### Spec
```bash
 {
    "rating": {
      "quality": {
          "value": "SecondaryRatings.quality.Value",     
          "max": "SecondaryRatings.quality.RatingRange" 
      }   
    }
}
```

###### Salida
```bash
  {
    "SecondaryRatings": {
      "quality": {
        "Value": 3,
        "RatingRange": 5
      }
    }
  }
```

### Uso de comodines 

###### Entrada
```bash
{
  "rating": {
      "primary": {
          "value": 3,  
          "max": 5      
      },
      "quality": {    
          "value": 3,  
          "max": 5     
      },
      "sharpness": {   
          "value": 7,  
          "max": 10   
      }
  }
}
```

###### Spec
```bash
{
  "rating": {
    "primary": {
        "value": "Rating",                       
        "max": "RatingRange"                     
    },
    "*": {                                        
        "value": "SecondaryRatings.&1.Value",                                 
        "max": "SecondaryRatings.&1.Range",      
        "$": "SecondaryRatings.&1.Id"                                                                                                        
    }
  }
}
```

###### Salida
```bash
{
  "Rating": 3,
  "RatingRange": 5,
  "SecondaryRatings": {
     "quality": {
        "Range": 5,
        "Value": 3,
        "Id": "quality"    
     },
     "sharpness": {
        "Range": 10,
        "Value": 7,
        "Id": "sharpness"  
     }
  }
}
```

### default

###### Entrada
```bash
{
  "Rating":3,
  "SecondaryRatings":{
     "quality":{
        "Range":7,
        "Value":3,
        "Id":"quality"
     },
     "sharpness": {
        "Value":4,
        "Id":"sharpness"
     }
  }
}
```

###### Spec
```bash
{
  "RatingRange" : 5,
  "SecondaryRatings": {
    "quality|value" : {
       "ValueLabel": null,
       "Label": null,
       "MaxLabel": "Great",
       "MinLabel": "Terrible",
       "DisplayType": "NORMAL"
    },
    "*": {
       "Range" : 5,
       "ValueLabel": null,
       "Label": null,
       "MaxLabel": "High",
       "MinLabel": "Low",
       "DisplayType": "NORMAL"
    }
  }
}
```

###### Salida
```bash
{
  "Rating":3,
  "RatingRange" : 5,
  "SecondaryRatings":{
     "quality":{
        "Range":7,
        "Value":3,
        "Id":"quality",
        "ValueLabel": null,
        "Label": null,
        "MaxLabel": "Great",
        "MinLabel": "Terrible",
        "DisplayType": "NORMAL"
     },
     "sharpness": {
        "Range":5,
        "Value":4,
        "Id":"sharpness",
        "ValueLabel": null,
        "Label": null,
        "MaxLabel": "High",
        "MinLabel": "Low",
        "DisplayType": "NORMAL"
     }
  }
```


### remove

###### Entrada
```bash
{
  "~emVersion" : "2",
  "id":"123124",
  "productId":"31231231",
  "submissionId":"34343",
  "this" : "stays",
  "configured" : {
    "a" : "b",
    "c" : "d"
  }
}
```

###### Spec
```bash
{
  "~emVersion" : "",
  "productId":"",
  "submissionId":"",
  "configured" : {
    "c" : ""
  }
}
```

###### Salida
```bash
{
  "id":"123124",
  "this" : "stays",
  "configured" : {
    "a" : "b"
  }
}
```

### cardinality
La tranformacion cardinality es tada por 2 valores:
```bash
UNO : si el valor de entrada es una lista, tome el primer elemento de esa lista. Para los demas tipos solo los almacena
MANY : guarda el elemento en una lista, si el elemento ya es una lista solo guarda el valor.
```
###### Entrada
```bash
{
  "views" : [
    { "count" : 1024 },
    { "count" : 2048 }
  ]
}
```

###### Spec
```bash
{
  "views" : {
    "@" : "ONE",
    "count" : "MANY"
  }
}
```

###### Salida
```bash
{
  "views" : {
    "count" : [ 1024 ]
  }
}
```

##### Transformaciones de modificacion
Las transformaciones de modificación, como su nombre lo indica, son las que tienen como objetivo modificar o alterar los valores dentro del JSON. Para la modificación de los valores se utilizan métodos nativos de java.

```bash
modify-overwrite-beta:This esta variante del modificador crea que falta la clave/índice y sobrescribe el valor si está presente
modify-default-beta:This esta variante del modificador solo escribe cuando falta la clave/índice o el valor es nulo
modify-define-beta:This esta variante del modificador solo escribe cuando falta la clave/índice
```
###### Metodos
```bash
private static final Map<String, Function> STOCK_FUNCTIONS = new HashMap<>(  );

    static {
        STOCK_FUNCTIONS.put( "toLower", new Strings.toLowerCase() );
        STOCK_FUNCTIONS.put( "toUpper", new Strings.toUpperCase() );
        STOCK_FUNCTIONS.put( "concat", new Strings.concat() );
        STOCK_FUNCTIONS.put( "join", new Strings.join() );
        STOCK_FUNCTIONS.put( "split", new Strings.split() );
        STOCK_FUNCTIONS.put( "substring", new Strings.substring() );
        STOCK_FUNCTIONS.put( "trim", new Strings.trim() );
        STOCK_FUNCTIONS.put( "leftPad", new Strings.leftPad() );
        STOCK_FUNCTIONS.put( "rightPad", new Strings.rightPad() );

        STOCK_FUNCTIONS.put( "min", new Math.min() );
        STOCK_FUNCTIONS.put( "max", new Math.max() );
        STOCK_FUNCTIONS.put( "abs", new Math.abs() );
        STOCK_FUNCTIONS.put( "avg", new Math.avg() );
        STOCK_FUNCTIONS.put( "intSum", new Math.intSum() );
        STOCK_FUNCTIONS.put( "doubleSum", new Math.doubleSum() );
        STOCK_FUNCTIONS.put( "longSum", new Math.longSum() );
        STOCK_FUNCTIONS.put( "intSubtract", new Math.intSubtract() );
        STOCK_FUNCTIONS.put( "doubleSubtract", new Math.doubleSubtract() );
        STOCK_FUNCTIONS.put( "longSubtract", new Math.longSubtract() );
        STOCK_FUNCTIONS.put( "divide", new Math.divide() );
        STOCK_FUNCTIONS.put( "divideAndRound", new Math.divideAndRound() );


        STOCK_FUNCTIONS.put( "toInteger", new Objects.toInteger() );
        STOCK_FUNCTIONS.put( "toDouble", new Objects.toDouble() );
        STOCK_FUNCTIONS.put( "toLong", new Objects.toLong() );
        STOCK_FUNCTIONS.put( "toBoolean", new Objects.toBoolean() );
        STOCK_FUNCTIONS.put( "toString", new Objects.toString() );
        STOCK_FUNCTIONS.put( "size", new Objects.size() );

        STOCK_FUNCTIONS.put( "squashNulls", new Objects.squashNulls() );
        STOCK_FUNCTIONS.put( "recursivelySquashNulls", new Objects.recursivelySquashNulls() );

        STOCK_FUNCTIONS.put( "noop", Function.noop );
        STOCK_FUNCTIONS.put( "isPresent", Function.isPresent );
        STOCK_FUNCTIONS.put( "notNull", Function.notNull );
        STOCK_FUNCTIONS.put( "isNull", Function.isNull );

        STOCK_FUNCTIONS.put( "firstElement", new Lists.firstElement() );
        STOCK_FUNCTIONS.put( "lastElement", new Lists.lastElement() );
        STOCK_FUNCTIONS.put( "elementAt", new Lists.elementAt() );
        STOCK_FUNCTIONS.put( "toList", new Lists.toList() );
        STOCK_FUNCTIONS.put( "sort", new Lists.sort() );
    }
   
```

Ejemplo de transformaciones de modificacion:
###### Entrada
```bash
{
  "x": [ 3, 2, 1, "go"  ],
  "small": "small",
  "BIG": "BIG",

  "people": [
    {
      "firstName": "Bob",
      "lastName": "Smith",
      "address": {
        "state": null
      }
    },
    {
      "firstName": "Sterling",
      "lastName": "Archer"
    }
  ]
}
```

###### Spec
```bash
[
  {
    "operation": "modify-default-beta",
    "spec": {
      "y": "=join(',',@(1,x))",
      "small_toUpper": "=toUpper(@(1,small))",
      "BIG_toLower": "=toLower(@(1,BIG))",
      "people": {
        "*": {
          "fullName": "=concat(@(1,firstName),' ',@(1,lastName))",
          "address?": {
            "state": "Texas"
          }
        }
      }
    }
  }
]
```

###### Salida
```bash
{
  "x" : [ 3, 2, 1, "go" ],
  "small" : "small",
  "BIG" : "BIG",
  "people" : [ {
    "firstName" : "Bob",
    "lastName" : "Smith",
    "address" : {
      "state" : "Texas"
    },
    "fullName" : "Bob Smith"
  }, {
    "firstName" : "Sterling",
    "lastName" : "Archer",
    "fullName" : "Sterling Archer"
  } ],
  "y" : "3,2,1,go",
  "z" : "3 2 1 go",
  "small_toUpper" : "SMALL",
  "BIG_toLower" : "big"
}

```
* [Jolt](https://cool-cheng.blogspot.com/2019/12/json-jolt-tutorial.html)
* [Jolt Transform Demo Using](https://jolt-demo.appspot.com/#inception)
