import classNames from "classnames";
import PropTypes from "prop-types";
import {CheckLg as AcceptIcon, XLg as XIcon} from "react-bootstrap-icons";
import BootstrapTable from "react-bootstrap-table-next";
import filterFactory, {textFilter} from "react-bootstrap-table2-filter";

import {actionBtn, actions} from '../../global-styles/table-styles.module.scss';
import {tableClass} from "../../helpers/react-bootstrap-table2/tableClass";

export const TeamRequestsList = ({
    requests,
    acceptRequest,
    rejectRequest,
}) => {
    const actionFormatter = (cell, row) => {
        const cancelBtnClass = classNames(actionBtn, 'text-danger');
        const acceptBtnClass = classNames(actionBtn, 'text-success');
        return (
            <div className={actions}>
                <button
                    title='Zaakceptuj wniosek'
                    className={acceptBtnClass}
                    onClick={() => acceptRequest(row.id)}
                >
                    <AcceptIcon/>
                </button>

                <button
                    title='Odrzuć wniosek'
                    className={cancelBtnClass}
                    onClick={() => rejectRequest(row.id)}
                >
                    <XIcon/>
                </button>
            </div>
        );
    }

    const columns = [
        {
            dataField: 'id',
            hidden: true,
        },
        {
            dataField: 'requester',
            text: 'Wnioskodawca',
            headerAlign: 'center',
            align: 'center',
            filter: textFilter({
                id: 'requesterTeamRequestListFilter',
                placeholder: 'Filtruj...',
                delay: 0,
            }),
            sort: true,
            style: {verticalAlign: 'middle'},
        },
        {
            dataField: 'period',
            text: 'Termin',
            headerAlign: 'center',
            align: 'center',
            sort: true,
            style: {verticalAlign: 'middle'},
        },
        {
            dataField: 'actions',
            text: 'Akcje',
            headerAlign: 'center',
            formatter: actionFormatter,
            align: 'center',
            style: {verticalAlign: 'middle'},
        },
    ];

    return (
        <BootstrapTable
            bootstrap4
            keyField='id'
            data={requests}
            wrapperClasses={tableClass}
            columns={columns}
            filter={filterFactory()}
            filterPosition='top'
            bordered={false}
            hover
        />
    );
}

TeamRequestsList.propTypes = {
    requests: PropTypes.array,
    acceptRequest: PropTypes.func.isRequired,
    rejectRequest: PropTypes.func.isRequired,
}

TeamRequestsList.defaultProps = {
    requests: [],
}