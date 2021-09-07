import PropTypes from "prop-types";
import {useEffect} from "react";

import {acceptRequest, rejectRequest} from "../../../contexts/request-context/actions/changeRequestStatus";
import {fetchCompanyRequests} from "../../../contexts/request-context/actions/fetchCompanyRequests";
import {useRequests} from "../../../contexts/request-context/requestContext";
import {requestPeriodFormatter} from "../../../helpers/react-bootstrap-table2/RequestMapperHelper";
import {CompanyRequestsList} from "../CompanyRequestsList";

export const CompanyRequestsListWrapper = ({shouldFetchCompanyRequests}) => {
    const [state, requestsDispatch] = useRequests()
    const {requests, fetching} = state.companyRequests

    useEffect(() => {
        if (shouldFetchCompanyRequests) {
            fetchCompanyRequests(requestsDispatch)
        }
    }, [requestsDispatch, shouldFetchCompanyRequests])

    const formattedRequests = requests.map(req => {
        return {
            id: req.id,
            applicant: req.requesterName,
            examiner: req.acceptances.map(acc => acc.leaderName),
            period: requestPeriodFormatter(req),
        }
    })

    return (
        <CompanyRequestsList
            requests={formattedRequests}
            rejectRequest={(requestId) => rejectRequest(requestsDispatch, {requestId})}
            acceptRequest={(requestId) => acceptRequest(requestsDispatch, {requestId})}
            isFetching={fetching}
        />
    )
};

CompanyRequestsListWrapper.propTypes = {
    shouldFetchCompanyRequests: PropTypes.bool.isRequired,
}