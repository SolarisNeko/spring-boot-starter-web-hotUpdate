# spring-boot-starter-web-hotUpdate

Spring Boot - Hot Update.

## Maven
```xml
<dependency>
    <groupId>com.neko233</groupId>
    <artifactId>spring-boot-starter-web-hotUpdate</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Gradle
```groovy
implementation 'com.neko233:spring-boot-starter-web-hotUpdate:0.0.1'
```


# Use
## Java
```java

import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class HotUpdateController {
    
    @Resource
    private SpringHotUpdateHelper springHotUpdateHelper;
    
}
```

## Kotlin
```kotlin
@RestController
class Person constructor() {
    
    @Resource
    var springHotUpdateHelper: SpringHotUpdateHelper;

}
```