import React from 'react';

const GREY = '#8696a0';
const BLUE = '#53bdeb';

const SingleTick = ({ color }) => (
  <svg width="16" height="11" viewBox="0 0 16 11" fill="none">
    <path
      d="M11.0715 0.653321L4.99998 7.41332L2.24165 4.49999L1.16665 5.63332L4.99998 9.67332L12.1541 1.77999L11.0715 0.653321Z"
      fill={color}
    />
  </svg>
);

const DoubleTick = ({ color }) => (
  <svg width="20" height="11" viewBox="0 0 20 11" fill="none">
    <path
      d="M14.4047 0.653321L8.33314 7.41332L5.57481 4.49999L4.49981 5.63332L8.33314 9.67332L15.4873 1.77999L14.4047 0.653321Z"
      fill={color}
    />
    <path
      d="M18.7379 0.653321L12.6664 7.41332L11.9997 6.71332L10.9247 7.84666L12.6664 9.67332L19.8206 1.77999L18.7379 0.653321Z"
      fill={color}
    />
  </svg>
);

const MessageTicks = ({ status }) => {
  if (status === 'READ') {
    return <DoubleTick color={BLUE} />;
  }
  if (status === 'DELIVERED') {
    return <DoubleTick color={GREY} />;
  }
  return <SingleTick color={GREY} />;
};

export default MessageTicks;