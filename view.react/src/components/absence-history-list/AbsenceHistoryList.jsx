import PropTypes from "prop-types";
import {useEffect, useState} from "react";
import {Dropdown, DropdownButton} from "react-bootstrap";
import {useLocation} from "react-router-dom";

import {getCurrentUser} from "../../api/services/session.service";
import {useAbsenceHistory} from "../../contexts/absence-history-context/absenceHistoryContext";
import {formatLogs} from "../../helpers/AbsenceHistoryFormatterHelper";
import {getPaginationForPage} from "../../helpers/pagination/PaginationHelper";
import styles from "./AbsenceHistoryList.module.scss";
import {AbsenceHistoryTab} from "./AbsenceHistoryTab";

export const AbsenceHistoryList = ({fetchHistoryLogs, setPageNumber}) => {
    const [state, absenceHistoryDispatch] = useAbsenceHistory()
    const {absenceHistory, absenceHistoryPage} = state;
    const {ec: isUserEC} = getCurrentUser();

    const location = useLocation();

    const FIRST_AVAILABLE_YEAR = 2016;
    const currentYear = (new Date()).getFullYear()
    const availableYears = getAvailableYears(FIRST_AVAILABLE_YEAR, currentYear);

    const [selectedYear, setSelectedYear] = useState(currentYear);
    const [currentSort, setCurrentSort] = useState({field: "created", order: "desc"})

    useEffect(() => {
        fetchHistoryLogs(absenceHistoryDispatch, {
            selectedYear,
            sortField: currentSort.field,
            sortOrder: currentSort.order
        })
    }, [absenceHistoryDispatch, selectedYear, fetchHistoryLogs, currentSort]);

    const handleYearChange = (newYear) => {
        setSelectedYear(newYear);
    }

    const formattedLogs = formatLogs(absenceHistory);
    let vacationTypeLabel = isUserEC ? "Pozostały urlop" : "Pozostała przerwa"

    let header = 'Historia użytkownika';
    if (location.state?.fullName && location.pathname !== '/history/me') {
        header = header.concat(` - ${location.state.fullName}`);
        vacationTypeLabel = location.state.vacationTypeLabel;
    }

    const pagination = getPaginationForPage({
        page: absenceHistoryPage,
        onClick: pageNumber => setPageNumber(pageNumber)
    })

    return (
        <>
            <div className={styles.panelFooter}>
                <h3>{header}</h3>
                <div>
                    <DropdownButton id="dropdown-basic-button"
                                    title={selectedYear}
                                    size="sm"
                                    bsPrefix={styles.datesDropdown}
                                    onSelect={year => handleYearChange(year)}>
                        {availableYears.map((val, index) => (
                            <Dropdown.Item className={styles.dropItem}
                                           key={index}
                                           eventKey={val}
                            >
                                {val}
                            </Dropdown.Item>
                        ))}
                    </DropdownButton>
                </div>
            </div>
            <AbsenceHistoryTab
                logs={formattedLogs}
                vacationTypeLabel={vacationTypeLabel}
                setSort={(sort) => setCurrentSort(sort)}
            />
            {pagination}
        </>
    );
}

const getAvailableYears = (startYear, currentYear) => {
    const years = [];
    for (let i = currentYear; i >= startYear; i--) {
        years.push(i);
    }
    return years;
}

AbsenceHistoryList.propTypes = {
    fetchHistoryLogs: PropTypes.func.isRequired,
}
