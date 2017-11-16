# Change Log

## [Unreleased](https://github.com/muoncore/newton/tree/HEAD)

[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.13...HEAD)

**Implemented enhancements:**

- Remove mongo [\#23](https://github.com/muoncore/newton/issues/23)

**Closed issues:**

- test-event-store:0.0.11-SNAPSHOT not available in Artifactory for netwon-spring-starter [\#70](https://github.com/muoncore/newton/issues/70)

**Merged pull requests:**

- Separation of Mongo event store from Newton Core [\#72](https://github.com/muoncore/newton/pull/72) ([robpurcell](https://github.com/robpurcell))

## [v0.0.13](https://github.com/muoncore/newton/tree/v0.0.13) (2017-10-13)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.12...v0.0.13)

## [v0.0.12](https://github.com/muoncore/newton/tree/v0.0.12) (2017-10-13)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.11...v0.0.12)

**Fixed bugs:**

- Locking Process failed with exception [\#62](https://github.com/muoncore/newton/issues/62)
- Fails to persist domain events: photon is unavailable [\#54](https://github.com/muoncore/newton/issues/54)

**Closed issues:**

- Add event relationships into event meta data [\#67](https://github.com/muoncore/newton/issues/67)
- Update to generate full pom for maven central [\#65](https://github.com/muoncore/newton/issues/65)

**Merged pull requests:**

- Correction in README [\#69](https://github.com/muoncore/newton/pull/69) ([fiq](https://github.com/fiq))
- 0.12 series  [\#68](https://github.com/muoncore/newton/pull/68) ([daviddawson](https://github.com/daviddawson))

## [v0.0.11](https://github.com/muoncore/newton/tree/v0.0.11) (2017-09-02)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.10...v0.0.11)

**Implemented enhancements:**

- "Error with registering a saga in the start cache" log statement is misleading [\#53](https://github.com/muoncore/newton/issues/53)
- Add a method to add an event to an aggregate without fully loading it [\#2](https://github.com/muoncore/newton/issues/2)

**Fixed bugs:**

- Starting client app when photon is unavailable causes undesirable failures [\#51](https://github.com/muoncore/newton/issues/51)
- Unable to load aggregate even though it exists [\#40](https://github.com/muoncore/newton/issues/40)

**Closed issues:**

- In event replay, use RS back pressure to smooth replay and stop buffering [\#63](https://github.com/muoncore/newton/issues/63)
- Don't use exceptions for flow control on aggregate repository [\#60](https://github.com/muoncore/newton/issues/60)
- Newton shouldn't initialize if spring-app is unable to start [\#59](https://github.com/muoncore/newton/issues/59)
- Less verbose 'error' logs upon lost connections [\#58](https://github.com/muoncore/newton/issues/58)
- CommandBus should return CompleteableFuture to enable easy integration with Spring web async support. [\#56](https://github.com/muoncore/newton/issues/56)
- On command execution failure in a saga, take the failure events and pass them back to the saga [\#49](https://github.com/muoncore/newton/issues/49)
- Remove SagaBus and replace with a Saga Registry [\#17](https://github.com/muoncore/newton/issues/17)

**Merged pull requests:**

- Smooth event replay by using RS signals [\#66](https://github.com/muoncore/newton/pull/66) ([daviddawson](https://github.com/daviddawson))

## [v0.0.10](https://github.com/muoncore/newton/tree/v0.0.10) (2017-06-28)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.9...v0.0.10)

**Implemented enhancements:**

- Aggregates with shared Ids try to load events that's not part of the aggregate [\#47](https://github.com/muoncore/newton/issues/47)

**Fixed bugs:**

- Aggregates with shared Ids try to load events that's not part of the aggregate [\#47](https://github.com/muoncore/newton/issues/47)
- Sagas are being started more than once for the same event [\#46](https://github.com/muoncore/newton/issues/46)
- Event not on classpath throws Nullpointer when processed outside originating bounded context [\#45](https://github.com/muoncore/newton/issues/45)
- Endless loop when submitting an event during another event being processed [\#44](https://github.com/muoncore/newton/issues/44)
- Restarting photon causes any further event processing to fail on clients [\#41](https://github.com/muoncore/newton/issues/41)
- Debugging using breakpoints causes any further event processing from happening [\#22](https://github.com/muoncore/newton/issues/22)

**Closed issues:**

- Saga lifecycle subscription seems to always subscribe from the start of the stream [\#48](https://github.com/muoncore/newton/issues/48)
- Add a LocalOnlyLockService  [\#39](https://github.com/muoncore/newton/issues/39)
- Allow deleting of aggregate roots via repo.delete [\#38](https://github.com/muoncore/newton/issues/38)
- Add a return path for the Command Bus [\#37](https://github.com/muoncore/newton/issues/37)

**Merged pull requests:**

- 0.0.10 milestone complete [\#50](https://github.com/muoncore/newton/pull/50) ([daviddawson](https://github.com/daviddawson))

## [v0.0.9](https://github.com/muoncore/newton/tree/v0.0.9) (2017-05-15)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.8...v0.0.9)

**Implemented enhancements:**

- Remove @NewtonView for now in favour of an abstract method on BaseView [\#24](https://github.com/muoncore/newton/issues/24)

**Fixed bugs:**

- Extend eventstreamprocessor with regards to deserialisation of TenantEvent if unwrapped.  [\#26](https://github.com/muoncore/newton/issues/26)

**Merged pull requests:**

- Rework APIs, make use more consistent. Extend hierarchies with domain services.  [\#33](https://github.com/muoncore/newton/pull/33) ([daviddawson](https://github.com/daviddawson))

## [v0.0.8](https://github.com/muoncore/newton/tree/v0.0.8) (2017-05-15)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.7...v0.0.8)

**Implemented enhancements:**

- Remove spring auto-config scanning & replace with @EnableNewton [\#30](https://github.com/muoncore/newton/issues/30)
- Introduce abstract Base Domain Service [\#29](https://github.com/muoncore/newton/issues/29)

## [v0.0.7](https://github.com/muoncore/newton/tree/v0.0.7) (2017-05-10)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.6...v0.0.7)

## [v0.0.6](https://github.com/muoncore/newton/tree/v0.0.6) (2017-05-10)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.5...v0.0.6)

**Implemented enhancements:**

- Remove AggregateRootId type in favour of an annotation on the event class [\#25](https://github.com/muoncore/newton/issues/25)

**Closed issues:**

- Default context name to be the application name in aggregateroots [\#27](https://github.com/muoncore/newton/issues/27)
- Add in default codec for DocumentId [\#19](https://github.com/muoncore/newton/issues/19)

**Merged pull requests:**

- Rework IDs, extensions [\#28](https://github.com/muoncore/newton/pull/28) ([daviddawson](https://github.com/daviddawson))

## [v0.0.5](https://github.com/muoncore/newton/tree/v0.0.5) (2017-05-02)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.4...v0.0.5)

**Closed issues:**

- Should only create the repository class if it does not already exist [\#21](https://github.com/muoncore/newton/issues/21)
- Photon 'from' is being misused [\#20](https://github.com/muoncore/newton/issues/20)
- Provide a cold+hot subscription to an event aggregate [\#16](https://github.com/muoncore/newton/issues/16)
- Expose aggregate stream as a stream [\#15](https://github.com/muoncore/newton/issues/15)
- Need to sync up the stream names for UniqueAggregateDomainService [\#5](https://github.com/muoncore/newton/issues/5)

## [v0.0.4](https://github.com/muoncore/newton/tree/v0.0.4) (2017-04-24)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.3...v0.0.4)

**Closed issues:**

- Initiate saga processing direct from am event [\#12](https://github.com/muoncore/newton/issues/12)

## [v0.0.3](https://github.com/muoncore/newton/tree/v0.0.3) (2017-04-15)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.2...v0.0.3)

**Closed issues:**

- Reflections requires explicit jar file classpath loading in spring boot apps [\#14](https://github.com/muoncore/newton/issues/14)

## [v0.0.2](https://github.com/muoncore/newton/tree/v0.0.2) (2017-04-07)
[Full Changelog](https://github.com/muoncore/newton/compare/v0.0.1...v0.0.2)

**Closed issues:**

- Auto generate repositories [\#11](https://github.com/muoncore/newton/issues/11)
- Add view types: RebuildingDatastoreView, SharedDataStore, BaseView [\#10](https://github.com/muoncore/newton/issues/10)
- Automatically generate the repositories [\#4](https://github.com/muoncore/newton/issues/4)

## [v0.0.1](https://github.com/muoncore/newton/tree/v0.0.1) (2017-04-06)
**Merged pull requests:**

- Initial newton [\#9](https://github.com/muoncore/newton/pull/9) ([daviddawson](https://github.com/daviddawson))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*