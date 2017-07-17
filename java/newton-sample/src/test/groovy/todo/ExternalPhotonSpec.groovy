package todo

import com.google.gson.Gson
import io.muoncore.newton.EventHandler
import io.muoncore.newton.NewtonEvent
import io.muoncore.newton.SampleApplication
import io.muoncore.newton.StreamSubscriptionManager
import io.muoncore.newton.TodoSaga
import io.muoncore.newton.domainservice.EventDrivenDomainService
import io.muoncore.newton.eventsource.EventTypeNotFound
import io.muoncore.newton.eventsource.muon.MuonEventSourceRepository
import io.muoncore.newton.saga.SagaFactory
import io.muoncore.newton.saga.SagaRepository
import io.muoncore.newton.support.DocumentId
import io.muoncore.newton.support.TenantContextHolder
import io.muoncore.newton.todo.Task
import io.muoncore.newton.todo.TenantEvent
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.client.EventClient
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@ActiveProfiles(["test", "functional"])
@SpringBootTest(classes = [MuonTestConfig, SampleApplication, FailConfig])
class MissingEventSpec extends Specification {

  @Autowired
  MyEventService eventService

  @Autowired
  EventClient eventClient

  @Autowired
  MuonEventSourceRepository<Task> repo

  @Autowired
  SagaRepository sagaRepository

  def "can replay aggregate streams in parallel"() {

    Task task = repo.newInstance { new Task(new DocumentId(), "Hi!") }


    sleep 1000
    when:

    def num = new AtomicInteger(1)

    def latch = new CountDownLatch(40)

    20.times {

      def exec = {
        try {
          TenantContextHolder.setTenantId("hello")
          Task t = repo.load(task.id)
          t.changeDescription("Hello" + num.addAndGet(1))
          repo.save(t)
          TenantContextHolder.setTenantId(null)
        } finally {
          latch.countDown()
          println "COUNTDOWN $num"
        }
      }

      exec()
      exec()
//      Thread.start exec
//      Thread.start exec
    }

    latch.await()
//    sleep(500)

    task = repo.load(task.id)

    then:
    task.description == "Hello41"
  }

  def "event service can emit events"() {
    when:
    println "EMIT WAS " + eventClient.event(ClientEvent.ofType(FirstEvent.simpleName).stream("newstream").payload(new FirstEvent(id:"12345")).build()).status

    then:
    new PollingConditions().eventually {
      eventService.ev2?.id
    }
  }

  def "can start a saga via an event"() {
    when:
    def events = repo.save(new Task(new DocumentId(), "Hi!"))

    println "Event is $events"
    sleep(500)
    def sagas = sagaRepository.getSagasCreatedByEventId(events[0].id)

    then:
    sagas.size() == 1
    sagas[0] instanceof TodoSaga
  }

  def "emit large objects"() {
    when:
    500.times {
      println "EMIT WAS " + eventClient.event(ClientEvent.ofType("Awesome").stream("newstream").payload(BIG).build()).status
    }
    then:
    new PollingConditions().eventually {
      eventService.ev2
    }
  }

  static def BIG = new Gson().fromJson("""
{"stream-name":"awesome", "payload":[{"_id":"59653534f354fa8f17242a89","index":0,"guid":"b3e4acf1-e20f-4999-9724-bfc2133d030f","isActive":false,"balance":"\$2,061.54","picture":"http://placehold.it/32x32","age":36,"eyeColor":"blue","name":"Wyatt Jacobson","gender":"male","company":"XYQAG","email":"wyattjacobson@xyqag.com","phone":"+1 (904) 559-2453","address":"566 Osborn Street, Richford, Tennessee, 3809","about":"Nisi enim exercitation ullamco in cillum ipsum velit amet nulla nostrud tempor irure. Ut qui sint Lorem occaecat nulla velit velit irure Lorem id. Nulla officia dolore esse amet labore exercitation. Nisi tempor ad aute ad in officia deserunt occaecat esse dolore qui aliqua deserunt irure. Labore reprehenderit exercitation esse proident. Commodo sint ullamco adipisicing duis mollit elit in anim in dolore minim incididunt deserunt.\\r\\n","registered":"2015-06-06T09:18:34 -01:00","latitude":31.98106,"longitude":-117.394201,"tags":["ullamco","magna","do","reprehenderit","reprehenderit","nulla","culpa"],"friends":[{"id":0,"name":"Lucas Mooney"},{"id":1,"name":"Gwendolyn Townsend"},{"id":2,"name":"Marcia Cherry"}],"greeting":"Hello, Wyatt Jacobson! You have 1 unread messages.","favoriteFruit":"apple"},{"_id":"59653534c87ec3990bbd678e","index":1,"guid":"138c5b05-cbe3-42c0-ae8d-94b82656e2ec","isActive":false,"balance":"\$3,671.19","picture":"http://placehold.it/32x32","age":36,"eyeColor":"green","name":"Miranda Waters","gender":"male","company":"PORTALINE","email":"mirandawaters@portaline.com","phone":"+1 (908) 570-3279","address":"185 Nevins Street, Brambleton, Louisiana, 6789","about":"Pariatur ad reprehenderit fugiat dolor irure ipsum deserunt exercitation et duis pariatur nisi fugiat eiusmod. Consectetur consequat Lorem dolor ea consequat aliquip dolore ad dolor incididunt exercitation laboris esse. Cillum ut amet reprehenderit sit dolore dolor pariatur.\\r\\n","registered":"2014-09-12T01:41:57 -01:00","latitude":-29.466002,"longitude":-164.88761,"tags":["occaecat","velit","incididunt","deserunt","adipisicing","sint","Lorem"],"friends":[{"id":0,"name":"Monique Gilmore"},{"id":1,"name":"Mcgee Beach"},{"id":2,"name":"Joni Dunn"}],"greeting":"Hello, Miranda Waters! You have 6 unread messages.","favoriteFruit":"strawberry"},{"_id":"59653534da3fe368f668edaa","index":2,"guid":"d376628a-d2cd-4b71-80bc-cc13de877d70","isActive":true,"balance":"\$2,259.98","picture":"http://placehold.it/32x32","age":25,"eyeColor":"green","name":"April Colon","gender":"female","company":"RECOGNIA","email":"aprilcolon@recognia.com","phone":"+1 (832) 422-2937","address":"254 Cumberland Walk, Smeltertown, Ohio, 7172","about":"Ipsum amet exercitation tempor dolor ad. Excepteur dolore consequat anim ipsum tempor consectetur duis amet aute. Magna occaecat fugiat ullamco deserunt consequat excepteur. Laboris occaecat in officia labore ut fugiat minim officia occaecat. Et ipsum enim esse nostrud ullamco tempor anim. Proident pariatur mollit nostrud cupidatat officia dolore esse elit. Nisi qui fugiat nostrud non aute est Lorem ullamco sint ipsum eu ex.\\r\\n","registered":"2015-11-26T11:40:53 -00:00","latitude":-36.274157,"longitude":-75.795262,"tags":["nulla","aute","deserunt","proident","nisi","sint","exercitation"],"friends":[{"id":0,"name":"Chambers Vaughn"},{"id":1,"name":"Eleanor Dyer"},{"id":2,"name":"Brandie Stout"}],"greeting":"Hello, April Colon! You have 2 unread messages.","favoriteFruit":"apple"},{"_id":"596535346fa8c283b033b28d","index":3,"guid":"1ce38b23-6c69-4c50-be2c-135e06c3e548","isActive":true,"balance":"\$3,896.86","picture":"http://placehold.it/32x32","age":30,"eyeColor":"blue","name":"Mckinney Cline","gender":"male","company":"TUBALUM","email":"mckinneycline@tubalum.com","phone":"+1 (875) 543-2688","address":"465 Alice Court, Rosburg, Idaho, 6378","about":"Dolor excepteur exercitation excepteur qui consectetur consectetur non cillum anim. Commodo officia eu consectetur labore deserunt deserunt enim eu aliqua consectetur quis officia aliqua est. Sit ut ea labore amet anim id. Veniam occaecat non occaecat cupidatat officia irure labore laborum proident in dolore in.\\r\\n","registered":"2015-11-18T03:09:33 -00:00","latitude":-38.690121,"longitude":-93.458517,"tags":["pariatur","dolor","officia","proident","nostrud","id","anim"],"friends":[{"id":0,"name":"Lorena Barton"},{"id":1,"name":"Brooks Peck"},{"id":2,"name":"Pearson Ratliff"}],"greeting":"Hello, Mckinney Cline! You have 5 unread messages.","favoriteFruit":"apple"},{"_id":"596535348150b05bfa80befc","index":4,"guid":"587f7a70-c10a-47c7-82e3-cd00ad8bca53","isActive":false,"balance":"\$3,487.62","picture":"http://placehold.it/32x32","age":21,"eyeColor":"brown","name":"Reilly Cabrera","gender":"male","company":"VINCH","email":"reillycabrera@vinch.com","phone":"+1 (896) 557-3956","address":"711 Tapscott Avenue, Hessville, Maryland, 214","about":"Reprehenderit est non Lorem minim irure labore ullamco magna sit labore in anim. Fugiat occaecat esse id laboris cillum in ad deserunt veniam. Est occaecat mollit anim dolore aliquip laboris minim nulla. Labore aliquip ullamco mollit eu reprehenderit deserunt ad est dolore ut id duis. Elit non minim minim ad labore.\\r\\n","registered":"2014-06-03T10:27:02 -01:00","latitude":-62.08656,"longitude":52.953944,"tags":["aliquip","nostrud","ea","sint","elit","ea","ad"],"friends":[{"id":0,"name":"Betsy Stephens"},{"id":1,"name":"Johanna Kennedy"},{"id":2,"name":"Adkins Hughes"}],"greeting":"Hello, Reilly Cabrera! You have 5 unread messages.","favoriteFruit":"apple"},{"_id":"59653534b7a86068edb34e5d","index":5,"guid":"707aa0be-76a1-4f31-9f8b-84df0aefb5de","isActive":true,"balance":"\$2,926.10","picture":"http://placehold.it/32x32","age":26,"eyeColor":"blue","name":"Rogers Gould","gender":"male","company":"AMTAP","email":"rogersgould@amtap.com","phone":"+1 (865) 539-2288","address":"978 Knight Court, Zortman, Alabama, 2838","about":"In ut qui mollit nulla officia amet est ipsum sint velit cillum magna. Magna laboris nulla occaecat nulla est incididunt labore. Ea dolor laborum nulla nisi enim aliqua ad adipisicing. Cillum sit adipisicing consectetur ullamco velit cillum incididunt consectetur elit. Ad voluptate culpa eu aliqua laboris. Reprehenderit veniam velit nostrud tempor nostrud elit nulla Lorem eu proident deserunt sint. Cillum sit laborum eiusmod consectetur fugiat.\\r\\n","registered":"2017-05-19T01:45:35 -01:00","latitude":-5.158762,"longitude":-75.023681,"tags":["laborum","nulla","occaecat","proident","do","nulla","excepteur"],"friends":[{"id":0,"name":"Nicholson Shields"},{"id":1,"name":"Stacie Irwin"},{"id":2,"name":"Marshall Salazar"}],"greeting":"Hello, Rogers Gould! You have 4 unread messages.","favoriteFruit":"strawberry"},{"_id":"5965353446dc3ab8eeb85b41","index":6,"guid":"aea22056-c75f-4369-b2b8-24a743fb2dfe","isActive":false,"balance":"\$2,580.20","picture":"http://placehold.it/32x32","age":38,"eyeColor":"brown","name":"Patel Waller","gender":"male","company":"HOMELUX","email":"patelwaller@homelux.com","phone":"+1 (981) 412-3939","address":"476 Hemlock Street, Fairhaven, Indiana, 2907","about":"Ullamco ad labore ex et ex adipisicing fugiat sit reprehenderit incididunt ea anim qui nulla. Nulla ullamco pariatur proident enim dolore consectetur culpa minim. Esse ad cupidatat sunt laboris fugiat laboris id aliqua ad. Eu voluptate nostrud eiusmod nulla sunt dolore aute proident culpa.\\r\\n","registered":"2017-04-10T04:39:50 -01:00","latitude":6.675232,"longitude":-1.116621,"tags":["laboris","consequat","enim","Lorem","consectetur","occaecat","cupidatat"],"friends":[{"id":0,"name":"Donna Tran"},{"id":1,"name":"Vanessa Merritt"},{"id":2,"name":"Franks Johns"}],"greeting":"Hello, Patel Waller! You have 8 unread messages.","favoriteFruit":"banana"},{"_id":"59653534f3792ceff6a64546","index":7,"guid":"ac378b1e-d85d-4e4b-8b8f-181a8bb97a46","isActive":false,"balance":"\$2,008.59","picture":"http://placehold.it/32x32","age":30,"eyeColor":"green","name":"Pennington Bass","gender":"male","company":"EWAVES","email":"penningtonbass@ewaves.com","phone":"+1 (907) 474-2595","address":"508 Nixon Court, Jeff, Oklahoma, 9799","about":"Velit sit do id nulla. Excepteur id fugiat sit est. Irure anim adipisicing aliquip laborum sunt do duis culpa anim veniam ea.\\r\\n","registered":"2017-05-07T04:37:03 -01:00","latitude":-24.024087,"longitude":-89.554647,"tags":["excepteur","non","nisi","amet","adipisicing","magna","qui"],"friends":[{"id":0,"name":"Carol Pena"},{"id":1,"name":"Hazel Thornton"},{"id":2,"name":"Barrera Wagner"}],"greeting":"Hello, Pennington Bass! You have 5 unread messages.","favoriteFruit":"strawberry"},{"_id":"59653534d38d43ae10673d90","index":8,"guid":"5796d759-49ba-45c4-80e7-5e0729653a02","isActive":false,"balance":"\$2,720.84","picture":"http://placehold.it/32x32","age":40,"eyeColor":"blue","name":"Camille Carver","gender":"female","company":"MANUFACT","email":"camillecarver@manufact.com","phone":"+1 (953) 567-2192","address":"451 Schaefer Street, Norvelt, Virgin Islands, 9545","about":"Veniam cupidatat dolore do deserunt commodo sunt aliqua et cupidatat cupidatat laboris nisi excepteur aute. Dolor sunt commodo occaecat irure irure laboris duis eu veniam occaecat laboris mollit. Nisi reprehenderit tempor consequat in proident est ut duis culpa exercitation reprehenderit laboris ut proident. Nostrud velit ex amet Lorem sunt ea mollit veniam. Anim ad eiusmod ullamco officia exercitation ea irure.\\r\\n","registered":"2016-08-18T10:38:58 -01:00","latitude":-10.973148,"longitude":-145.882928,"tags":["sunt","id","dolor","ex","culpa","dolore","consectetur"],"friends":[{"id":0,"name":"Ronda Durham"},{"id":1,"name":"Lourdes Clarke"},{"id":2,"name":"Deanne Hardin"}],"greeting":"Hello, Camille Carver! You have 7 unread messages.","favoriteFruit":"strawberry"},{"_id":"5965353420991785f5b62142","index":9,"guid":"b69a1cbe-d11b-4c7e-9241-6b21d9cec926","isActive":true,"balance":"\$1,309.03","picture":"http://placehold.it/32x32","age":27,"eyeColor":"brown","name":"Waters Strong","gender":"male","company":"IMKAN","email":"watersstrong@imkan.com","phone":"+1 (942) 402-2181","address":"628 Broadway , Sanders, Arkansas, 5414","about":"Cillum sit pariatur irure et veniam enim aliqua tempor officia pariatur deserunt elit ex. Mollit cupidatat eu nostrud elit exercitation magna. Veniam ut nulla mollit laboris ex fugiat officia irure deserunt nostrud. Sunt cupidatat nostrud laboris id aute occaecat qui aute commodo sit. Velit ex velit dolor ut irure ut enim id aute nostrud irure dolore ex anim.\\r\\n","registered":"2014-06-19T12:50:56 -01:00","latitude":25.123428,"longitude":136.274996,"tags":["sint","nostrud","velit","sint","quis","officia","elit"],"friends":[{"id":0,"name":"Mack Robertson"},{"id":1,"name":"Priscilla Leonard"},{"id":2,"name":"Jones Landry"}],"greeting":"Hello, Waters Strong! You have 2 unread messages.","favoriteFruit":"strawberry"},{"_id":"5965353422e1bb833c223ac6","index":10,"guid":"ead29439-fa9d-4511-b72c-c189f40ff5f5","isActive":false,"balance":"\$2,009.58","picture":"http://placehold.it/32x32","age":26,"eyeColor":"green","name":"Arlene Pugh","gender":"female","company":"ZYTREX","email":"arlenepugh@zytrex.com","phone":"+1 (879) 564-2074","address":"777 Downing Street, Kipp, District Of Columbia, 4230","about":"Laboris minim exercitation minim cillum qui dolore sunt nulla nostrud ea laborum in voluptate. Laboris nostrud deserunt ipsum nostrud aute incididunt ex reprehenderit ad reprehenderit fugiat. Reprehenderit reprehenderit est duis nostrud sit aliqua commodo nisi occaecat fugiat qui nisi dolor. Sunt non proident eu reprehenderit exercitation officia aliqua qui.\\r\\n","registered":"2016-08-21T02:36:16 -01:00","latitude":-78.395056,"longitude":-75.838586,"tags":["sunt","fugiat","excepteur","consequat","voluptate","commodo","commodo"],"friends":[{"id":0,"name":"Gallagher Mccoy"},{"id":1,"name":"Chang Moody"},{"id":2,"name":"Peterson Morrow"}],"greeting":"Hello, Arlene Pugh! You have 9 unread messages.","favoriteFruit":"strawberry"},{"_id":"59653534fca1aab5f2f73503","index":11,"guid":"3f02b798-d031-4273-952d-cde9fee8ad86","isActive":true,"balance":"\$2,992.05","picture":"http://placehold.it/32x32","age":37,"eyeColor":"blue","name":"Blankenship Fowler","gender":"male","company":"EMOLTRA","email":"blankenshipfowler@emoltra.com","phone":"+1 (810) 487-3661","address":"109 Ford Street, Caroline, Wisconsin, 5701","about":"Occaecat occaecat elit ipsum nisi commodo voluptate id excepteur. Enim commodo labore id deserunt anim. Commodo fugiat nostrud do consectetur irure esse in Lorem. Adipisicing sint ipsum commodo reprehenderit elit deserunt.\\r\\n","registered":"2014-10-28T01:33:17 -00:00","latitude":69.304201,"longitude":176.340298,"tags":["deserunt","voluptate","magna","fugiat","excepteur","nisi","elit"],"friends":[{"id":0,"name":"Paulette Harris"},{"id":1,"name":"Kris Bean"},{"id":2,"name":"Evangelina Conrad"}],"greeting":"Hello, Blankenship Fowler! You have 5 unread messages.","favoriteFruit":"apple"},{"_id":"59653534543c3cce5fbd0195","index":12,"guid":"1baa7048-eacb-4d02-896a-8788c841bd33","isActive":false,"balance":"\$1,080.67","picture":"http://placehold.it/32x32","age":40,"eyeColor":"green","name":"Watkins Duffy","gender":"male","company":"ARTWORLDS","email":"watkinsduffy@artworlds.com","phone":"+1 (955) 527-2117","address":"327 Tehama Street, Hackneyville, Palau, 4033","about":"Ea eu id non et. Elit reprehenderit nostrud laboris occaecat esse. Pariatur nulla eiusmod consequat ea laborum ad esse incididunt nulla nostrud non dolor irure. Incididunt ex exercitation ut in deserunt amet ut ullamco ipsum deserunt pariatur eiusmod. Qui reprehenderit cupidatat minim exercitation ullamco laborum nisi.\\r\\n","registered":"2015-02-04T03:32:06 -00:00","latitude":87.213581,"longitude":100.602144,"tags":["sint","eiusmod","laboris","non","ut","ipsum","officia"],"friends":[{"id":0,"name":"Kerr Morin"},{"id":1,"name":"Forbes Mccray"},{"id":2,"name":"Stephenson Howard"}],"greeting":"Hello, Watkins Duffy! You have 4 unread messages.","favoriteFruit":"apple"}]}
""", Map)
}


@Configuration
class FailConfig {

  @Bean MyEventService ev(StreamSubscriptionManager s) { return new MyEventService(s)}

}

@Component
class MyEventService extends EventDrivenDomainService {

  @Autowired
  EventClient client

  EventTypeNotFound ev;
  SecondEvent ev2

  MyEventService(StreamSubscriptionManager streamSubscriptionManager) {
    super(streamSubscriptionManager)
    println "Made a service!"
  }

  @EventHandler
  void on(EventTypeNotFound eventTypeNotFound){
    this.ev = eventTypeNotFound;
  }

  @EventHandler
  void on(FirstEvent eventTypeNotFound){
    println "Got MyEvent"
    client.event(ClientEvent.ofType(SecondEvent.simpleName).stream("newstream").payload(new SecondEvent(id:"12345")).build())
  }

  @EventHandler
  void on(SecondEvent eventTypeNotFound){
    println "Got other event"
    ev2 = eventTypeNotFound
  }

  @Override
  protected String[] getStreams() {
    ["mystream", "newstream"]
  }
}


class SecondEvent extends TenantEvent<String> {
  String id
}

class FirstEvent extends TenantEvent<String> {
  String id
}
