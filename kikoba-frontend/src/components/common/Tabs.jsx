import clsx from 'clsx';
import './Tabs.css';

const Tabs = ({ tabs, active, onChange }) => (
  <div className="tabs" role="tablist">
    {tabs.map((tab) => (
      <button
        key={tab.key}
        type="button"
        role="tab"
        aria-selected={active === tab.key}
        className={clsx('tabs-item', active === tab.key && 'active')}
        onClick={() => onChange(tab.key)}
      >
        {tab.label}
      </button>
    ))}
  </div>
);

export default Tabs;