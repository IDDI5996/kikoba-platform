import React from 'react';

const Logo = ({ variant = 'full', className = '', ...props }) => {
  // If variant is 'icon', we only render the monogram part
  if (variant === 'icon') {
    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 120 120"
        className={className}
        style={{ height: '100%', width: 'auto', maxHeight: '48px', ...props.style }}
        {...props}
      >
        <defs>
          <linearGradient id="accentGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#00B4D8" stopOpacity="1" />
            <stop offset="100%" stopColor="#00D4FF" stopOpacity="1" />
          </linearGradient>
          <filter id="shadow" x="-10%" y="-10%" width="130%" height="130%">
            <feDropShadow dx="2" dy="4" stdDeviation="4" floodColor="#000000" floodOpacity="0.15" />
          </filter>
        </defs>

        <g transform="translate(10, 10)" filter="url(#shadow)">
          {/* Outer circle uses currentColor */}
          <circle cx="50" cy="50" r="38" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.3" />
          <circle cx="50" cy="50" r="28" fill="none" stroke="url(#accentGrad)" strokeWidth="2" opacity="0.15" />

          {/* K uses currentColor, C stays Cyan */}
          <text x="32" y="60" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="40" fontWeight="800" fill="currentColor" letterSpacing="-2">
            K
          </text>
          <text x="58" y="60" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="40" fontWeight="300" fill="url(#accentGrad)" letterSpacing="-2">
            C
          </text>

          <rect x="46" y="38" width="8" height="8" fill="#FFD700" transform="rotate(45, 50, 42)" opacity="0.8" />
        </g>
      </svg>
    );
  }

  // Full Logo (Icon + Text)
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 400 120"
      className={className}
      style={{ height: '100%', width: 'auto', maxHeight: '48px', ...props.style }}
      {...props}
    >
      <defs>
        <linearGradient id="accentGradFull" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#00B4D8" stopOpacity="1" />
          <stop offset="100%" stopColor="#00D4FF" stopOpacity="1" />
        </linearGradient>
        <filter id="shadowFull" x="-10%" y="-10%" width="130%" height="130%">
          <feDropShadow dx="2" dy="4" stdDeviation="4" floodColor="#000000" floodOpacity="0.15" />
        </filter>
      </defs>

      {/* Icon Part */}
      <g transform="translate(10, 10)" filter="url(#shadowFull)">
        <circle cx="50" cy="50" r="38" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.3" />
        <circle cx="50" cy="50" r="28" fill="none" stroke="url(#accentGradFull)" strokeWidth="2" opacity="0.15" />

        <text x="32" y="60" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="40" fontWeight="800" fill="currentColor" letterSpacing="-2">
          K
        </text>
        <text x="58" y="60" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="40" fontWeight="300" fill="url(#accentGradFull)" letterSpacing="-2">
          C
        </text>
        <rect x="46" y="38" width="8" height="8" fill="#FFD700" transform="rotate(45, 50, 42)" opacity="0.8" />
      </g>

      {/* Text Part */}
      <g transform="translate(100, 25)">
        {/* Uses currentColor for text */}
        <text x="0" y="38" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="34" fontWeight="700" fill="currentColor" letterSpacing="-0.5">
          Kikoba
        </text>

        <rect x="0" y="46" width="30" height="3" rx="1.5" fill="url(#accentGradFull)" />

        {/* Uses currentColor with opacity for the tagline */}
        <text x="0" y="68" fontFamily="'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif" fontSize="13" fontWeight="400" fill="currentColor" opacity="0.65" letterSpacing="0.5">
          Smart Savings for Communities
        </text>
      </g>
    </svg>
  );
};

export default Logo;