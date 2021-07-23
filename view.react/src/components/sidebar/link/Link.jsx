import PropTypes from 'prop-types';
import {NavLink} from 'react-router-dom';

import styles from './Link.module.scss';

export const Link = ({
    to,
    children,
}) => {
    return (
        <NavLink to={to} activeClassName={styles.activeLink}>
            {children}
        </NavLink>
    )
}

Link.propTypes = {
    to: PropTypes.string.isRequired,
}