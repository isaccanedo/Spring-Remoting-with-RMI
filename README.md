## Spring Remoting com RMI

# 1. Visão Geral
Java Remote Method Invocation permite invocar um objeto residente em uma Java Virtual Machine diferente. É uma tecnologia bem estabelecida, mas um pouco complicada de usar, como podemos ver na trilha oficial da Oracle dedicada ao assunto.

Neste artigo rápido, vamos explorar como o Spring Remoting permite alavancar o RMI de uma maneira mais fácil e limpa.

Este artigo também completa a visão geral do Spring Remoting. Você pode encontrar detalhes sobre outras tecnologias com suporte nas partes anteriores: Invokers HTTP, JMS, AMQP, Hessian e Burlap.

# 2. Dependências Maven
Como fizemos em nossos artigos anteriores, vamos configurar alguns aplicativos Spring Boot: um servidor que expõe o objeto chamável remoto e um cliente que invoca o serviço exposto.

Tudo o que precisamos está no jar de contexto spring - para que possamos trazê-lo usando qualquer auxiliar do Spring Boot que preferirmos - porque nosso principal objetivo é apenas ter as bibliotecas principais disponíveis.

Vamos agora prosseguir com o usual spring-boot-starter-web - lembrando de remover a dependência do Tomcat para excluir o serviço da web incorporado:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

# 3. Aplicativo de servidor
Começaremos declarando uma interface que define um serviço para reservar uma corrida de táxi, que será eventualmente exposta aos clientes:

```
public interface CabBookingService {
    Booking bookRide(String pickUpLocation) throws BookingException;
}
```

Em seguida, definiremos um bean que implementa a interface. Este é o bean que realmente executará a lógica de negócios no servidor:

```
@Bean 
CabBookingService bookingService() {
    return new CabBookingServiceImpl();
}
```

Vamos continuar declarando o Exportador que disponibiliza o serviço aos clientes. Nesse caso, usaremos o RmiServiceExporter:

```
@Bean 
RmiServiceExporter exporter(CabBookingService implementation) {
    Class<CabBookingService> serviceInterface = CabBookingService.class;
    RmiServiceExporter exporter = new RmiServiceExporter();
    exporter.setServiceInterface(serviceInterface);
    exporter.setService(implementation);
    exporter.setServiceName(serviceInterface.getSimpleName());
    exporter.setRegistryPort(1099); 
    return exporter;
}
```

Por meio de setServiceInterface(), fornecemos uma referência à interface que será chamada remotamente.

Devemos também fornecer uma referência ao objeto que está executando o método com setService(). Em seguida, poderíamos fornecer a porta do registro RMI disponível na máquina onde o servidor é executado se não quisermos usar a porta padrão 1099.

Devemos também definir um nome de serviço, que permita identificar o serviço exposto no registro RMI.

Com a configuração fornecida, o cliente poderá entrar em contato com o CabBookingService no seguinte URL:rmi://HOST:1199/CabBookingService.

Vamos finalmente iniciar o servidor. Nem mesmo precisamos iniciar o registro RMI por nós mesmos, porque o Spring fará isso automaticamente para nós se esse registro não estiver disponível.

# 4. Aplicativo do cliente
Vamos escrever agora o aplicativo cliente.

Começamos declarando o RmiProxyFactoryBean que criará um bean que possui a mesma interface exposta pelo serviço em execução no lado do servidor e que encaminhará de forma transparente as invocações que receberá para o servidor:

```
@Bean 
RmiProxyFactoryBean service() {
    RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
    rmiProxyFactory.setServiceUrl("rmi://localhost:1099/CabBookingService");
    rmiProxyFactory.setServiceInterface(CabBookingService.class);
    return rmiProxyFactory;
}
```

Vamos então escrever um código simples que inicializa o aplicativo cliente e usa o proxy definido na etapa anterior:

```
public static void main(String[] args) throws BookingException {
    CabBookingService service = SpringApplication
      .run(RmiClient.class, args).getBean(CabBookingService.class);
    Booking bookingOutcome = service
      .bookRide("13 Seagate Blvd, Key Largo, FL 33037");
    System.out.println(bookingOutcome);
}
```

Agora basta lançar o cliente para verificar se ele invoca o serviço exposto pelo servidor.

# 5. Conclusão
Neste tutorial, vimos como poderíamos usar Spring Remoting para facilitar o uso de RMI que de outra forma exigirá uma série de tarefas tediosas como, entre todas, girar um registro e definir serviços usando interfaces que fazem uso pesado de exceções verificadas.
