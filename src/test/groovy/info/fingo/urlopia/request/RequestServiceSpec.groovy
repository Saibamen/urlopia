package info.fingo.urlopia.request

import info.fingo.urlopia.api.v2.calendar.AbsentUserOutput
import info.fingo.urlopia.api.v2.exceptions.UnauthorizedException
import info.fingo.urlopia.api.v2.presence.PresenceConfirmationService
import info.fingo.urlopia.config.persistance.filter.Filter
import info.fingo.urlopia.history.HistoryLogService
import info.fingo.urlopia.request.absence.SpecialAbsenceRequestService
import info.fingo.urlopia.request.normal.NormalRequestService
import info.fingo.urlopia.request.normal.RequestTooFarInThePastException
import info.fingo.urlopia.team.Team
import info.fingo.urlopia.user.User
import info.fingo.urlopia.user.UserService
import spock.lang.Specification

import java.time.LocalDate

class RequestServiceSpec extends Specification {
    def requesterId = 1L
    def notRequesterId = 2L
    def requester = Mock(User) {
        getId() >> requesterId
    }
    def notRequester = Mock(User) {
        getId() >> notRequesterId
    }
    def startDate = LocalDate.now()
    def endDate = LocalDate.now()
    def type = RequestType.NORMAL
    def workingDays = 1
    def typeInfo = null
    def status = Request.Status.PENDING

    def request = new Request(requester, startDate, endDate, workingDays, type, typeInfo, status)

    def requestRepository = Mock(RequestRepository) {
        findById(request.getId()) >> Optional.of(request)
    }
    def historyLogService = Mock(HistoryLogService)
    def userService = Mock(UserService)
    def presenceConfirmationService = Mock(PresenceConfirmationService)
    def requestService = new RequestService(requestRepository,
            historyLogService,
            userService,
            presenceConfirmationService)


    def "create() WHEN request is not special and startDate is before one month past SHOULD throw RequestTooFarInThePastException"() {
        given:
        def today = LocalDate.now()
        def requestStartDate = today.minusMonths(2)
        def type = RequestType.NORMAL
        def normalRequestService = Mock(NormalRequestService)
        type.setService(normalRequestService)

        and: "Valid requestInput"
        def requestInput = Mock(RequestInput)
        requestInput.getStartDate() >> requestStartDate
        requestInput.getType() >> type

        when:
        requestService.create(requesterId, requestInput)

        then:
        thrown(RequestTooFarInThePastException)
    }

    def "create() WHEN request is special and startDate is before one month past SHOULD not throw RequestTooFarInThePastException"() {
        given:
        def today = LocalDate.now()
        def requestStartDate = today.minusMonths(2)
        def type = RequestType.SPECIAL
        def specialRequestService = Mock(SpecialAbsenceRequestService)
        type.setService(specialRequestService)

        and: "Valid requestInput"
        def requestInput = Mock(RequestInput)
        requestInput.getStartDate() >> requestStartDate
        requestInput.getType() >> type

        when:
        requestService.create(requesterId, requestInput)

        then:
        notThrown(RequestTooFarInThePastException)
    }

    def "create() WHEN startDate is after one month past SHOULD not throw RequestTooFarInThePastException"() {
        given:
        def today = LocalDate.now()
        def type = RequestType.NORMAL
        def normalRequestService = Mock(NormalRequestService)
        type.setService(normalRequestService)

        and: "Valid requestInput"
        def requestInput = Mock(RequestInput)
        requestInput.getStartDate() >> today
        requestInput.getType() >> type

        when:
        requestService.create(requesterId, requestInput)

        then:
        notThrown(RequestTooFarInThePastException)
    }

    def "validateAdminPermissionAndAccept() when admin is accepting request should change request status to ACCEPTED"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            accept(request) >> request.setStatus(Request.Status.ACCEPTED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> true

        when: "Admin user is accepting request"
        requestService.validateAdminPermissionAndAccept(request.getId(), notRequester.getId())

        then: "Request status changed to ACCEPTED"
        request.getStatus() == Request.Status.ACCEPTED
    }

    def "validateAdminPermissionAndAccept() when not admin is accepting request should throw UnauthorizedException"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            accept(request) >> request.setStatus(Request.Status.ACCEPTED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> false


        when: "Not admin user is accepting request"
        requestService.validateAdminPermissionAndAccept(request.getId(), requester.getId())

        then: "Exception is thrown"
        thrown(UnauthorizedException)
    }

    def "validateAdminPermissionAndReject() when admin is rejecting request should change request status to REJECTED"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            reject(request) >> request.setStatus(Request.Status.REJECTED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> true

        when: "Admin user is rejecting request"
        requestService.validateAdminPermissionAndReject(request.getId())

        then: "Request status changed to REJECTED"
        request.getStatus() == Request.Status.REJECTED
    }

    def "validateAdminPermissionAndReject() when not admin is rejecting request should throw UnauthorizedException"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            reject(request) >> request.setStatus(Request.Status.REJECTED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> false

        when: "Not admin user is rejecting request"
        requestService.validateAdminPermissionAndReject(request.getId())

        then: "Exception is thrown"
        thrown(UnauthorizedException)
    }

    def "cancel() when admin is canceling request should change request status to CANCELED"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            cancel(request) >> request.setStatus(Request.Status.CANCELED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> true

        when: "Admin user is canceling request"
        requestService.cancel(request.getId(), notRequester.getId())

        then: "Request status changed to CANCELED"
        request.getStatus() == Request.Status.CANCELED
    }

    def "cancel() when requester is canceling request should change request status to CANCELED"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            cancel(request) >> request.setStatus(Request.Status.CANCELED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> false

        when: "Requester is canceling request"
        requestService.cancel(request.getId(), requester.getId())

        then: "Request status changed to CANCELED"
        request.getStatus() == Request.Status.CANCELED
    }

    def "cancel() when not admin nor requester is canceling request should throw UnauthorizedException"() {
        given:
        def normalRequestService = Mock(NormalRequestService) {
            cancel(request) >> request.setStatus(Request.Status.CANCELED)
        }
        type.setService(normalRequestService)
        userService.isCurrentUserAdmin() >> false

        when: "Not admin user nor requester is canceling request"
        requestService.cancel(request.getId(), notRequester.getId())

        then: "Exception is thrown"
        thrown(UnauthorizedException)
    }

    def "accept() WHEN request is accepted SHOULD delete presence confirmations in its date range"() {
        given: "a normal request service that accepts given request"
        def normalRequestService = Mock(NormalRequestService) {
            accept(_ as Request) >> {Request req -> req.setStatus(Request.Status.ACCEPTED)}
        }
        type.setService(normalRequestService)

        when: "a request is accepted"
        requestService.accept(request.getId(), 999L)

        then: "presence confirmation service is called"
        1 * presenceConfirmationService.deletePresenceConfirmations(requesterId, request.getStartDate(), request.getEndDate())
    }

    def "getById() when requestId is valid should return request"() {
        given:
        def requestId = 1L
        def request = Mock(Request) {
            getId() >> requestId
        }

        requestRepository.findById(request.getId()) >> Optional.of(request)


        when:
        def returnedRequest = requestService.getById(request.getId())

        then:
        returnedRequest == request
    }

    def "getVacations() SHOULD return list of absent users outputs"() {
        given:
        def date = LocalDate.now()
        def filter = Filter.from("")
        def fullName = "Jan Kowalski"
        def nameOfTeam = "ZPH"
        def users = [Mock(User) {
            getId() >> 1L
            getFullName() >> fullName
            getTeams() >> [Mock(Team) {
                getName() >> nameOfTeam
            }]
        }]
        def requests = [Mock(Request) {
            getStatus() >> Request.Status.ACCEPTED
        }]
        userService.get(filter) >> users
        requestRepository.findAll(_ as Filter) >> requests

        def expectedResult = [new AbsentUserOutput(fullName, [nameOfTeam])]

        when:
        def output = requestService.getVacations(date, filter)

        then:
        output == expectedResult
    }
}
